package net.detalk.api.controller.v1.response;

import lombok.Builder;

@Builder
public record GetMemberPublicProfileResponse(
    String userhandle,
    String nickname,
    String description,
    String avatarUrl
) {}
