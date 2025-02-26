package net.detalk.api.post.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.post.domain.exception.DuplicatedIdempotentKeyException;
import net.detalk.api.post.repository.ProductPostIdempotentRepository;
import net.detalk.api.support.util.UUIDGenerator;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductPostIdempotentService {

    private final ProductPostIdempotentRepository postIdempotentRepository;
    private final UUIDGenerator uuidGenerator;

    public boolean insertIdempotentKey(String idempotentKey, Instant now) {
        UUID uuidIdempotentKey = uuidGenerator.fromString(idempotentKey);
        boolean isNew = postIdempotentRepository.insertIdempotentKey(uuidIdempotentKey, now);
        if (!isNew) {
            throw new DuplicatedIdempotentKeyException();
        }
        return true;
    }
}
