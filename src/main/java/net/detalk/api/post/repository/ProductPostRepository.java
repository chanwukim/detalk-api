package net.detalk.api.post.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.detalk.api.post.controller.response.GetProductPostResponse;
import net.detalk.api.post.domain.ProductPost;

public interface ProductPostRepository {

    ProductPost save(Long writerId, Long productId, Instant now);

    Optional<ProductPost> findById(Long id);

    Optional<ProductPost> findByProductId(Long productId);

    Optional<GetProductPostResponse> findDetailsById(Long id);

    List<GetProductPostResponse> findProductPosts(int pageSize, Long nextId);

    List<GetProductPostResponse> findProductPostsByMemberId(Long memberId, int pageSize,
        Long nextId);

    List<GetProductPostResponse> findRecommendedPostsByMemberId(Long memberId, int pageSize,
        Long nextId);

    List<GetProductPostResponse> findProductPostsByTags(int pageSize, Long nextId,
        List<String> tags);


    boolean existsById(Long id);

    void incrementRecommendCount(Long postId, int count);




}
