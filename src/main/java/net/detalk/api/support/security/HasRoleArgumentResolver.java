package net.detalk.api.support.security;

import java.util.Arrays;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.auth.domain.exception.AccessDeniedException;
import net.detalk.api.auth.domain.exception.SessionUserNotFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link HasRole} 어노테이션을 처리하는 HandlerMethodArgumentResolver.
 * <p>
 * 메서드에 {@link HasRole} 어노테이션이 붙어 있을 경우,
 * 해당 메서드에 {@link SecurityUser} 객체를 주입하는 역할을 한다.
 * 인증된 사용자가 요청된 역할을 가지고 있는지 확인하고,
 * 필요한 경우 해당 {@link SecurityUser}를 반환한다.
 * </p>
 */
@Slf4j
@Component
public class HasRoleArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @HasRole 어노테이션 + SecurityUser 타입 파라미터인지 확인하여 동작
        return parameter.hasParameterAnnotation(HasRole.class)
            && SecurityUser.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(
        @NonNull MethodParameter parameter,
        ModelAndViewContainer mavContainer,
        @NonNull NativeWebRequest webRequest,
        WebDataBinderFactory binderFactory
    ) {

        /*
         * TokenFilter에서 SecurityContextHolder 설정을 확인할 것
         * @see net.detalk.api.support.security.TokenFilter
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // authentication 없거나 Principal이 SecurityUser 아닐경우
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            log.debug("Authentication failed for method: {}. authentication: {}", Objects.requireNonNull(
                parameter.getMethod()).getName(), authentication);
            throw new SessionUserNotFoundException();
        }

        // 컨트롤러 @HasRole 어노테이션이 요구하는 ROLE 가져오기
        HasRole hasRoleAnnotation = parameter.getParameterAnnotation(HasRole.class);
        assert hasRoleAnnotation != null;
        SecurityRole[] requiredRoles = hasRoleAnnotation.value();

        boolean hasAnyRole = Arrays.stream(requiredRoles)
            .map(SecurityRole::getName)
            .anyMatch(role -> hasRole(securityUser, role));

        // 사용자가 필요한 역할을 가지고 있는지 확인
        if (hasAnyRole) {
            return securityUser;
        }

        // 사용자는 인증되었지만 요청된 권한이 없거나 부족한 경우 접근 거부
        // 예: ADMIN 역할이 필요한 엔드포인트에 USER 역할로 접근 시도
        throw new AccessDeniedException();
    }

    private boolean hasRole(SecurityUser securityUser, String requiredRole) {
        return securityUser.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals(requiredRole));
    }
}
