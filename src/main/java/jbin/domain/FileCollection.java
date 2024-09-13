package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;

import java.util.UUID;

public record FileCollection(
        @Id UUID id,
        @DbName("file_id") UUID fileId,
        @DbName("collection_id") UUID collectionId
) {
}