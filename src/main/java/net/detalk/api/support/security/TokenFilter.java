package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
import net.detalk.api.support.error.ExpiredTokenException;
import net.detalk.api.support.util.CookieUtil;
import net.detalk.api.support.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import static net.detalk.api.support.util.Constant.COOKIE_ACCESS_TOKEN;

/**
 * 요청 헤더에서 액세스 토큰을 추출하고 인증을 처리하는 Filter.
 * <p>
 * HTTP 요청 헤더에서 액세스 토큰을 추출하여 검증한 후,
 * 유효한 토큰에 대해 {@link SecurityUser} 객체를 생성하고 Spring Security 인증 컨텍스트에 저장한다.
 * </p>
 */
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    public TokenFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return (path.equals("/api/v1/auth/refresh") && method.equalsIgnoreCase("POST")) ||
            (path.equals("/api/v1/auth/sign-out")  && method.equalsIgnoreCase("POST")) ||
            path.equals("/api/health");
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        log.debug("[doFilterInternal] {} {}", path, method);
        String accessToken = null;

        Optional<Cookie> cookieOptional = CookieUtil.getCookie(COOKIE_ACCESS_TOKEN, request);
        if (cookieOptional.isPresent()) {
            accessToken = cookieOptional.get().getValue();
            log.debug("[doFilterInternal] accessToken : {}", accessToken);
        }

        if(StringUtil.isNotEmpty(accessToken)) {
            try {
                AccessToken verifiedAccessToken = tokenProvider.parseAccessToken(accessToken);

                List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(verifiedAccessToken.getAuthorities());

                SecurityUser principal = new SecurityUser(verifiedAccessToken.getMemberId(), authorities);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null, // 비밀번호 없음
                    authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("[doFilterInternal] SecurityContext에 authentication 설정 완료");
            } catch (ExpiredTokenException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write(new ObjectMapper().writeValueAsString(new ErrorMessage(ErrorCode.TOKEN_EXPIRED)));
                response.getWriter().flush();
                return;
            } catch (Exception e) {
                log.error("[doFilterInternal] unknown : {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write(new ObjectMapper().writeValueAsString(new ErrorMessage(ErrorCode.TOKEN_INVALID)));
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
