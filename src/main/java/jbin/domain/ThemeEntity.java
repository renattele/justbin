package jbin.domain;

import jbin.orm.DbName;
import jbin.orm.Id;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ThemeEntity(
        @Id UUID id,
        String name,
        @DbName("foreground_color") String foregroundColor,
        @DbName("background_color") String backgroundColor,
        String css,
        UUID owner
) {
}
