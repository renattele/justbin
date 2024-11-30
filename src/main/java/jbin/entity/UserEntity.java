package jbin.entity;

import jbin.orm.DbName;
import jbin.orm.Id;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserEntity(
        @Id UUID id,
        String username,
        @DbName("password_hash") String passwordHash
) {
}