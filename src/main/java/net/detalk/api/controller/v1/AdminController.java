package net.detalk.api.controller.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.response.ActiveSessionResponse;
import net.detalk.api.service.SessionTrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SessionTrackingService sessionTrackingService;

    @Operation(summary = "접속중인 세션 목록 조회", description = "현재 접속중인 모든 사용자의 세션 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @GetMapping("/sessions")
    public ResponseEntity<List<ActiveSessionResponse>> getActiveSessions() {
        List<ActiveSessionResponse> activeSessions = sessionTrackingService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }


}
