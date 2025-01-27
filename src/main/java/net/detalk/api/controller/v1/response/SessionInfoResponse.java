package net.detalk.api.controller.v1.response;

import java.util.List;
import lombok.Builder;

@Builder
public record SessionInfoResponse(
    Long id,
    String userhandle,
    String nickname,
    String description,
    String avatarUrl,
    List<String> roles
) {

}
