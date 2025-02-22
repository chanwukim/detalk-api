package net.detalk.api.member.controller.v1;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import net.detalk.api.member.controller.v1.request.CreateMemberProfileRequest;
import net.detalk.api.member.controller.v1.request.UpdateMemberProfileRequest;
import net.detalk.api.member.controller.v1.response.GetMemberProfileResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.member.service.MemberService;
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
    public ResponseEntity<GetMemberProfileResponse> me(
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        GetMemberProfileResponse memberProfileResponse = memberService.me(user.getId());
        return ResponseEntity.ok().body(memberProfileResponse);
    }

    @PostMapping("/profile")
    public ResponseEntity<GetMemberProfileResponse> registerProfile(
        @HasRole(SecurityRole.MEMBER) SecurityUser user,
        @Valid @RequestBody CreateMemberProfileRequest registerProfile
    ) {
        GetMemberProfileResponse memberProfileResponse = memberService.registerProfile(user.getId(),
            registerProfile.userhandle(), registerProfile.nickname());
        return ResponseEntity.ok().body(memberProfileResponse);
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
        @HasRole(SecurityRole.MEMBER) SecurityUser user,
        @Valid @RequestBody UpdateMemberProfileRequest updateProfileRequest
    ) {
        memberService.updateProfile(user.getId(), updateProfileRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/posts")
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getMyPosts(
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId,
        @HasRole(SecurityRole.MEMBER) SecurityUser user
    ) {
        CursorPageData<GetProductPostResponse> myPostResponses = productPostService.getProductPostsByMemberId(
            user.getId(), pageSize, nextId);
        return ResponseEntity.ok(myPostResponses);
    }

    @GetMapping("/{userhandle}/posts")
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getPostsByUserHandle(
        @PathVariable("userhandle") String userhandle,
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId
    ) {
        Long memberId = memberService.getMemberIdByUserHandle(userhandle);

        CursorPageData<GetProductPostResponse> memberProductResponses =
            productPostService.getProductPostsByMemberId(memberId, pageSize, nextId);

        return ResponseEntity.ok(memberProductResponses);
    }

    @GetMapping("/{userhandle}/recommended-posts")
    public ResponseEntity<CursorPageData<GetProductPostResponse>> getRecommendedPosts(
        @PathVariable("userhandle") String userhandle,
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize,
        @RequestParam(name = "startId", required = false) Long nextId
    ) {
        Long memberId = memberService.getMemberIdByUserHandle(userhandle);

        CursorPageData<GetProductPostResponse> recommendedPostResponses =
            productPostService.getRecommendedPostsByMemberId(memberId, pageSize, nextId);

        return ResponseEntity.ok(recommendedPostResponses);
    }

    @GetMapping("/{userhandle}")
    public ResponseEntity<GetMemberProfileResponse> getMemberProfile(
        @PathVariable("userhandle") String userhandle
    ) {
        GetMemberProfileResponse memberProfileResponse = memberService.getMemberDetailByUserhandle(userhandle);
        return ResponseEntity.ok(memberProfileResponse);
    }
}
