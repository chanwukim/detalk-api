package net.detalk.api.geo.domain;


import lombok.Builder;

@Builder
public record GeoInfo(
    String continentCode,
    String countryIso,
    String countryName,
    String cityName
) {}
