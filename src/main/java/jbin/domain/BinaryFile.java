package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;

import java.time.Instant;
import java.util.UUID;

public record BinaryFile(
        @Id UUID id,
        String name,
        @DbName("creation_date") Instant creationDate,
        @DbName("last_updated_date") Instant lastUpdatedDate,
        boolean readonly,
        @DbName("content_type") String contentType
) {
}