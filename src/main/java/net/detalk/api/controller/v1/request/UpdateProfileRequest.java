package net.detalk.api.controller.v1.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UpdateProfileRequest(
    @NotBlank(message = "Userhandle is required")
    @Size(max = 64, message = "Userhandle must be less than or equal to 64 characters")
    String userandle,
    String avatarId,
    @NotBlank(message = "Nickname is required")
    @Size(min = 2, max = 20, message = "Nickname must be a minimum of 2 characters and a maximum of 20 characters")
    String nickname,
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    String description
) {}
