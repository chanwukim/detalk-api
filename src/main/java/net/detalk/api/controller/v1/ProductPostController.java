package net.detalk.api.controller.v1;

import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.response.CreateProductPostResponse;
import net.detalk.api.controller.v1.response.GetProductPostResponse;
import net.detalk.api.controller.v1.response.PageResponse;
import net.detalk.api.domain.ProductCreate;
import net.detalk.api.service.ProductPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products/posts")
@RequiredArgsConstructor
public class ProductPostController {

    private final ProductPostService productPostService;

    @PostMapping
    public ResponseEntity<CreateProductPostResponse> create(ProductCreate productCreate) {
        Long productPostId = productPostService.create(productCreate);
        return ResponseEntity.ok(new CreateProductPostResponse(productPostId));
    }

    @GetMapping
    public ResponseEntity<PageResponse<GetProductPostResponse>> getProductPosts(
        @RequestParam(name = "size", defaultValue = "5") @Max(20) int pageSize ,
        @RequestParam(name = "startId", required = false) Long nextId) {
        PageResponse<GetProductPostResponse> result = productPostService.getProductPosts(pageSize,
            nextId);
        return ResponseEntity.ok(result);
    }

}
