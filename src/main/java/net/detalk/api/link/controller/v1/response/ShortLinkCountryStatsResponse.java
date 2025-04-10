package net.detalk.api.link.controller.v1.response;

import java.util.List;
import net.detalk.api.link.domain.CountryStatPoint;

public record ShortLinkCountryStatsResponse(List<CountryStatPoint> stats) {

}
