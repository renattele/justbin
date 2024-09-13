package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.List;
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
            insert into theme (id, name, foreground_color, background_color, css, owner) values (?, ?, ?, ?, ?, ?)
            on conflict (id)
            do update set
            name = excluded.name,
            foreground_color = excluded.foreground_color,
            background_color = excluded.background_color,
            css = excluded.css
            """)
    UUID upsert(Theme theme);

    @Query("select * from theme")
    List<Theme> getAll();

    @Query("select * from theme where id = ?")
    Theme getById(UUID id);

    @Query("delete from theme where id = ?")
    boolean delete(UUID themeId);
}