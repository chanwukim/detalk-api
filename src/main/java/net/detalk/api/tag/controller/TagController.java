package net.detalk.api.tag.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.tag.controller.response.GetTagResponse;
import net.detalk.api.tag.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
@RestController
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<GetTagResponse>> getAllTags() {
        List<GetTagResponse> result = tagService.findAll();
        return ResponseEntity.ok(result);
    }

}
