package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;

import java.util.UUID;

public record Theme(
        @Id UUID id,
        String name,
        @DbName("foreground_color") String foregroundColor,
        @DbName("background_color") String backgroundColor,
        String css,
        UUID owner
) {
}
