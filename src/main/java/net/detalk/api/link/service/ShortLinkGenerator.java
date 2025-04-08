package net.detalk.api.link.service;

public interface ShortLinkGenerator {

    /**
     * 고유하고 URL-safe한 단축 링크 코드를 생성합니다.
     * @return 생성된 단축 코드 (예: "7z_XyWqP")
     */
    String generate();

}
