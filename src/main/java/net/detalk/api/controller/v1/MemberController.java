package net.detalk.api.controller.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.request.RegisterProfileRequest;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.domain.MemberDetail;
import net.detalk.api.service.MemberService;
import net.detalk.api.service.ProductPostService;
import net.detalk.api.support.CursorPageData;
import net.detalk.api.support.security.HasRole;
import net.detalk.api.support.security.SecurityRole;
import net.detalk.api.support.security.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ProductPostService productPostService;

    @GetMapping("/me")
    public ResponseEntity<MemberDetail> me(
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        MemberDetail memberDetail = memberService.me(user.getId());
        return ResponseEntity.ok().body(memberDetail);
    }

    @PostMapping("/profile")
    public ResponseEntity<MemberDetail> registerProfile(
        @HasRole(SecurityRole.MEMBER) SecurityUser user,
        @Valid @RequestBody RegisterProfileRequest registerProfile
    ) {
        MemberDetail memberDetail = memberService.registerProfile(user.getId(), registerProfile.userhandle(), registerProfile.nickname());
        return ResponseEntity.ok().body(memberDetail);
    }

    @GetMapping("/me/posts")
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getMyPosts(
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        CursorPageData<GetProductPostResponse> result = productPostService.getProductPostsByMemberId(
            user.getId(), pageSize, nextId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{userhandle}/posts")
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getPostsByUserHandle(
        @PathVariable("userhandle") String userhandle,
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId
    ) {
        Long memberId = memberService.findIdByUserHandle(userhandle);

        CursorPageData<GetProductPostResponse> posts =
            productPostService.getProductPostsByMemberId(memberId, pageSize, nextId);

        return ResponseEntity.ok(posts);
    }
}
