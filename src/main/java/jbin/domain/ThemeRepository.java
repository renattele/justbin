package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Table(name = "theme", createTable = """
        create table theme(
            id uuid primary key,
            name varchar(10000),
            foreground_color varchar(100),
            background_color varchar(100),
            css varchar(10000),
            owner uuid,
            foreign key (owner) references users(id)
        )
        """)
public interface ThemeRepository {
    @Query("""
            insert into theme (id, name, foreground_color, background_color, css, owner) values (:id, :name, :foregroundColor, :backgroundColor, :css, :owner)
            on conflict (id)
            do update set
            name = excluded.name,
            foreground_color = excluded.foreground_color,
            background_color = excluded.background_color,
            css = excluded.css
            """)
    Optional<UUID> upsert(ThemeEntity theme);

    @Query("""
            update theme set
            name = :name,
            foreground_color = :foregroundColor,
            background_color = :backgroundColor,
            css = :css
            where id = :id
""")
    boolean update(ThemeEntity theme);

    @Query("select * from theme")
    List<ThemeEntity> getAll();

    @Query("select * from theme where id = :id")
    Optional<ThemeEntity> getById(UUID id);

    @Query("delete from theme where id = :themeId")
    boolean delete(UUID themeId);
}