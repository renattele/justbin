package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record BinaryFileEntity(
        @Id UUID id,
        String name,
        @DbName("creation_date") Instant creationDate,
        @DbName("content_type") String contentType
) {
}