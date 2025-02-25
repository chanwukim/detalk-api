package net.detalk.api.tag.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.detalk.api.tag.controller.v1.response.GetTagResponse;
import net.detalk.api.tag.domain.Tag;
import net.detalk.api.tag.repository.TagRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TagService {

    private final TagRepository tagRepository;

    public List<GetTagResponse> findAll() {
        return tagRepository.findAll().stream()
            .map(tag -> new GetTagResponse(tag.getName()))
            .toList();
    }

    /**
     * 있다면 태그 가져오고 없으면 DB 저장
     * @param tagName 태그이름
     * @return 저장 혹은 조회된 태그
     */
    public Tag getOrCreateTag(String tagName) {
        return tagRepository.findByName(tagName)
            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
    }

}
