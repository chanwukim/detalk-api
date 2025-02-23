package net.detalk.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import net.detalk.api.tag.controller.response.GetTagResponse;
import net.detalk.api.tag.domain.Tag;
import net.detalk.api.tag.repository.TagRepository;
import net.detalk.api.tag.service.TagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;
    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository);
    }

    @DisplayName("[findAll] 모든 태그 조회 성공")
    @Test
    void findAll() {

        // given
        Tag tag1 = Tag.builder().name("태그1").build();
        Tag tag2 = Tag.builder().name("태그2").build();

        List<Tag> tags = List.of(tag1, tag2);

        when(tagRepository.findAll()).thenReturn(tags);

        // when
        List<GetTagResponse> result = tagService.findAll();


        // then
        assertThat(result)
            .hasSize(tags.size())
            .extracting("name")
            .containsExactly("태그1", "태그2");

    }

    @DisplayName("[getOrCreateTag] 존재하는 태그 조회")
    @Test
    void getOrTagWhenExistsThenGet() {

        // given
        String tagName = "태그1";
        Tag existingTag = Tag.builder().name(tagName).build();
        when(tagRepository.findByName(tagName)).thenReturn(Optional.of(existingTag));

        // when
        Tag result = tagService.getOrCreateTag(tagName);

        // then
        assertThat(result).isEqualTo(existingTag);

        // save 메서드가 호출되지 않았는지 확인
        verify(tagRepository, never()).save(any());
    }

    @DisplayName("[getOrCreateTag] 존재하지 않는 태그 조회")
    @Test
    void getOrTagWhenNotExistsThenSaveAndGet() {

        // given
        String tagName = "태그1";
        Tag newTag = Tag.builder().name(tagName).build();

        when(tagRepository.findByName(tagName)).thenReturn(Optional.empty());
        when(tagRepository.save(any(Tag.class))).thenReturn(newTag);

        // then
        Tag result = tagService.getOrCreateTag(tagName);

        assertThat(result).isEqualTo(newTag);

        // save 메서드가 호출되었는지 확인
        verify(tagRepository).save(argThat(tag ->
            tag.getName().equals(tagName)
        ));
    }



}