package net.detalk.api.controller.v1.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record CreateProductPostRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than or equal to 255 characters")
    String name,

    @NotBlank(message = "URL is required")
    @Size(max = 255, message = "URL must be less than or equal to 255 characters")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].\\S*$", message = "URL must be valid")
    String url,

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be less than or equal to 1000 characters")
    String description,

    @NotNull(message = "Image IDs cannot be null")
    List<@NotNull(message = "Image ID cannot be null") String> imageIds,

    boolean isMaker,

    @NotNull(message = "Tags cannot be null")
    @Size(max = 10, message = "Cannot have more than 10 tags")
    List<@NotBlank(message = "Tag cannot be blank") @Size(max = 32, message = "Tag must be less than or equal to 32 characters") String> tags,

    @NotBlank(message = "Pricing Plan is required")
    @Size(max = 255, message = "Pricing Plan must be less than or equal to 255 characters")
    String pricingPlan,

    @NotBlank(message = "idempotentKey is required")
    String idempotentKey

) {}

