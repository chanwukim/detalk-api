package net.detalk.api.controller.v1.request;

public record UpdateProfileRequest(
    String userHandle,
    String avatarId,
    String nickname,
    String description
) {}
