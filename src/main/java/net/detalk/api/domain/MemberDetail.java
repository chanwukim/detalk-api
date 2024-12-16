package net.detalk.api.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberDetail {
    private Long id;
    private String userhandle;
    private String nickname;
    private String description;
    // TODO: 아바타

    @Builder
    public MemberDetail(Long id, String userhandle, String nickname, String description) {
        this.id = id;
        this.userhandle = userhandle;
        this.nickname = nickname;
        this.description = description;
    }
}
