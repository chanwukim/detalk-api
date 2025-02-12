package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberDetail {
    private Long id;
    private String userhandle;
    private String nickname;
    private String description;
    private String avatarUrl;

    @Builder
    public MemberDetail(Long id, String userhandle, String nickname, String description,
        String avatarUrl) {
        this.id = id;
        this.userhandle = userhandle;
        this.nickname = nickname;
        this.description = description;
        this.avatarUrl = avatarUrl;
    }
}
