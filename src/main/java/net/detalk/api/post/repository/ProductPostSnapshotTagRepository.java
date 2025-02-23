package net.detalk.api.post.repository;

import java.util.List;
import net.detalk.api.post.domain.ProductPostSnapshotTag;

public interface ProductPostSnapshotTagRepository {

    void saveAll(List<ProductPostSnapshotTag> snapshotTags);

}
