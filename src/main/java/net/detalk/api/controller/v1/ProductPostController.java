package net.detalk.api.controller.v1;

import lombok.RequiredArgsConstructor;
import net.detalk.api.controller.v1.response.CreateProductPostResponse;
import net.detalk.api.domain.ProductCreate;
import net.detalk.api.service.ProductPostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
