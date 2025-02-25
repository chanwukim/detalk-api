package net.detalk.api.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.admin.controller.response.GetActiveSessionResponse;
import net.detalk.api.admin.controller.response.GetVisitorLogResponse;
import net.detalk.api.admin.service.SessionTrackingService;
import net.detalk.api.admin.service.VisitorLogService;
import net.detalk.api.support.paging.PagingData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
@RestController
@RequiredArgsConstructor
public class AdminController {

    private final SessionTrackingService sessionTrackingService;
    private final VisitorLogService visitorLogService;

    @Operation(summary = "접속중인 세션 목록 조회", description = "현재 접속중인 모든 사용자의 세션 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @GetMapping("/sessions")
    public ResponseEntity<List<GetActiveSessionResponse>> getActiveSessions() {
        List<GetActiveSessionResponse> activeSessions = sessionTrackingService.getActiveSessions();
        return ResponseEntity.ok(activeSessions);
    }

    @Operation(summary = "방문자 정보 목록 조회", description = "사이트에 방문한 회원의 위치 정보를 페이징하여 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0")
    @Parameter(name = "size", description = "페이지당 항목 수", example = "10")
    @GetMapping("/visitor-logs")
    public ResponseEntity<PagingData<GetVisitorLogResponse>> getVisitorLogs(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PagingData<GetVisitorLogResponse> result = visitorLogService.findAll(pageable);
        return ResponseEntity.ok(result);
    }

}
