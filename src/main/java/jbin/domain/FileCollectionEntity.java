package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;
import lombok.Builder;

import java.util.UUID;

@Builder
public record FileCollectionEntity(
        @Id UUID id,
        @DbName("file_id") UUID fileId,
        @DbName("collection_id") UUID collectionId
) {
}