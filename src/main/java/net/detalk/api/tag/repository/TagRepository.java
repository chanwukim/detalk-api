package net.detalk.api.tag.repository;

import java.util.List;
import java.util.Optional;
import net.detalk.api.tag.domain.Tag;

public interface TagRepository {

    Optional<Tag> findByName(String name);

    Tag save(Tag tag);

    List<Tag> findAll();

}
