package jbin.domain;

import jbin.orm.Id;

import java.util.UUID;

public record BinaryCollection(@Id UUID id, String name) {
}
