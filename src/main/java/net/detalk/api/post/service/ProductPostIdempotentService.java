package net.detalk.api.post.service;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.detalk.api.post.repository.ProductPostIdempotentRepository;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductPostIdempotentService {

    private final ProductPostIdempotentRepository postIdempotentRepository;

    public boolean insertIdempotentKey(UUID idempotentKey, Instant now) {
        return postIdempotentRepository.insertIdempotentKey(idempotentKey, now);
    }
}
