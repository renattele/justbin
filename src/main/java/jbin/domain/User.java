package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;

import java.util.UUID;

public record User(
        @Id UUID id,
        String username,
        @DbName("password_hash") String passwordHash
) {
}