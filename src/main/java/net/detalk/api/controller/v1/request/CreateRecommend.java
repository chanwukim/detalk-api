package net.detalk.api.controller.v1.request;

import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record CreateRecommend (

    @NotBlank(message = "Reason is required")
    @Size(max = 255, message = "Reason must be less than or equal to 255 characters")
    String reason

) {}
