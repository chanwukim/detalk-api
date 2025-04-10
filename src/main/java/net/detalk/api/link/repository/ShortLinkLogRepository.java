package net.detalk.api.link.repository;

import java.util.List;
import net.detalk.api.link.domain.CountryStatPoint;
import net.detalk.api.link.domain.ShortLinkLog;

public interface ShortLinkLogRepository {
    ShortLinkLog save(ShortLinkLog logEntry);

    /**
     * 지정된 링크 ID(linkId)에 대한 국가별 클릭 수를 조회합니다.
     *
     * @param linkId 통계를 조회할 short_links 테이블의 ID
     * @return 국가명과 클릭 수를 담은 CountryStatPoint 리스트
     */
    List<CountryStatPoint> getClickCountsByLinkId(long linkId);

}
