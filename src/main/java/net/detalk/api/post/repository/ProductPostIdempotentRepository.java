package net.detalk.api.post.repository;

import java.time.Instant;
import java.util.UUID;

public interface ProductPostIdempotentRepository {

    boolean insertIdempotentKey(UUID idempotentKey, Instant now);

}
