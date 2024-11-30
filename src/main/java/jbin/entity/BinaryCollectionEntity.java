package jbin.entity;

import jbin.orm.Id;
import lombok.Builder;

import java.util.UUID;

@Builder
public record BinaryCollectionEntity(@Id UUID id, String name) {
}
