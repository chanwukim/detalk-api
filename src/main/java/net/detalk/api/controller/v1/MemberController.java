package net.detalk.api.controller.v1;

import lombok.RequiredArgsConstructor;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.service.MemberService;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<MemberDetail> me(
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        MemberDetail memberDetail = memberService.me(user.getId());
        return ResponseEntity.ok().body(memberDetail);
    }
}
