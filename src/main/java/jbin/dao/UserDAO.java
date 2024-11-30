package jbin.dao;

import jbin.entity.UserEntity;
import jbin.orm.Query;
import jbin.orm.Table;

import java.util.Optional;
import java.util.UUID;

@Table(name = "users", createTable = """
        create table if not exists users
        (
            id uuid primary key,
            username varchar(1000),
            password_hash varchar(1000)
        )
        """)
public interface UserDAO {
    @Query("""
            insert into users (id, username, password_hash) values (:id, :username, :passwordHash)
            on conflict (id)
            do update set
            password_hash = excluded.password_hash;
            """)
    boolean upsert(UserEntity user);

    @Query("select * from users where username = :name")
    Optional<UserEntity> findByName(String name);

    @Query("select * from users where id = :id")
    Optional<UserEntity> findById(UUID id);

    @Query("delete from users where username = :name")
    boolean deleteByName(String name);
}