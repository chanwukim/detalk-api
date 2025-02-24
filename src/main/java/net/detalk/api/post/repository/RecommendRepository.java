package net.detalk.api.post.repository;

import java.time.Instant;
import java.util.Optional;
import net.detalk.api.post.domain.Recommend;

public interface RecommendRepository {

    Optional<Recommend> findByReason(String reason);

    Recommend save(String reason, Instant now);

}
