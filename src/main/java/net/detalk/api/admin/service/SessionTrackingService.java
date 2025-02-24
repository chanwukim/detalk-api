package net.detalk.api.admin.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.admin.controller.response.ActiveSessionResponse;
import net.detalk.api.support.security.oauth.OAuthUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

@PreAuthorize("hasRole('ADMIN')")
@Service
@RequiredArgsConstructor
public class SessionTrackingService {

    private final SessionRegistry sessionRegistry;

    /**
     * 현재 접속중인 세션 목록 조회
     */
    public List<ActiveSessionResponse> getActiveSessions() {
        return sessionRegistry.getAllPrincipals().stream()
            .filter(OAuthUser.class::isInstance)
            .map(OAuthUser.class::cast)
            .flatMap(user -> sessionRegistry.getAllSessions(user, false).stream()
                .map(sessionInfo -> new ActiveSessionResponse(
                    user.getId(),
                    user.getUsername(),
                    sessionInfo.getSessionId(),
                    sessionInfo.getLastRequest()
                ))
            ).toList();
    }
}
