package net.detalk.api.link.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ShortLinkLog {

    private Long id;
    private Long linkId;
    private Instant clickedAt;
    private String ipAddress;
    private String country;
    private String city;
    private String userAgent;
    private String referrer;
    private String deviceType;
    private String os;
    private String browser;

    @Builder
    public ShortLinkLog(Long id, Long linkId, Instant clickedAt, String ipAddress, String country,
        String city, String userAgent,
        String referrer, String deviceType, String os,
        String browser) {
        this.id = id;
        this.linkId = linkId;
        this.clickedAt = clickedAt;
        this.ipAddress = ipAddress;
        this.country = country;
        this.city = city;
        this.userAgent = userAgent;
        this.referrer = referrer;
        this.deviceType = deviceType;
        this.os = os;
        this.browser = browser;
    }

}
