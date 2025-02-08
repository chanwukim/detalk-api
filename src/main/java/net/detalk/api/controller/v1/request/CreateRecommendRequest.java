package net.detalk.api.controller.v1.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
public record CreateRecommendRequest(

    @NotEmpty(message = "At least one reason is required")
    List<@NotBlank(message = "Reason cannot be blank") @Size(max = 255, message = "Reason must be less than or equal to 255 characters") String> reasons,

    @NotBlank(message = "Recommendation content cannot be blank")
    @Size(max = 255, message = "Recommendation content must be less or equal to 500 characters")
    String content

) {
}