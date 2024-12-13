package net.detalk.api.support.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.support.error.ErrorCode;
import net.detalk.api.support.error.ErrorMessage;
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

@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    private final TokenProvider tokenProvider;

    public TokenFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        String accessToken = null;

        // Authorization 헤더에서 Bearer 토큰 추출
        String authHeader = request.getHeader("Authorization");
        if (StringUtil.isNotEmpty(authHeader) && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
            log.debug("[doFilterInternal] 요청 헤더에 액세스 토큰이 존재 : {}", accessToken);
        }

        if(StringUtil.isNotEmpty(accessToken)) {
            try {
                AccessToken verifiedAccessToken = tokenProvider.parseAccessToken(accessToken);

                List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(verifiedAccessToken.getAuthorities());

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    verifiedAccessToken.getMemberId(),
                    null, // 비밀번호 없음
                    authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                PrintWriter writer = response.getWriter();
                writer.write(new ObjectMapper().writeValueAsString(new ErrorMessage(ErrorCode.TOKEN_EXPIRED)));
                response.getWriter().flush();
                return;
            } catch (Exception e) {
                log.error("[doFilterInternal] unkown", e);
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
