package net.detalk.api.link.domain;

//  국가별 통계 (국가명, 클릭 수)
public record CountryStatPoint(String country, long count) {

    // new CountryStatPoint(...) 가 호출될 때마다 실행됨
    public CountryStatPoint {
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("country must not be null");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
    }

}

