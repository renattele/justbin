package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.UUID;

@Table(name = "users", createTable = """
        create table users
        (
            id uuid primary key,
            username varchar(1000),
            password_hash varchar(1000)
        )
        """)
public interface UserRepository {
    @Query("""
            insert into users (id, username, password_hash) values (?, ?, ?)
            on conflict (id)
            do update set
            password_hash = excluded.password_hash;
            """)
    boolean upsert(User user);

    @Query("select * from users where username = ?")
    User findByName(String name);

    @Query("select * from users where id = ?")
    User findById(UUID id);

    @Query("delete from users where username = ?")
    boolean deleteByName(String name);
}