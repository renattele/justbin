package jbin.dao;

import jbin.entity.BinaryCollectionEntity;
import jbin.orm.Query;
import jbin.orm.Table;

import java.util.Optional;
import java.util.UUID;

@Table(name = "binary_collection", createTable = """
        create table if not exists binary_collection
        (
            id uuid primary key,
            name varchar(10000)
        );
        """)
public interface BinaryCollectionDAO {
    @Query("""
            insert into binary_collection (id, name) values (:id, :name)
            on conflict (id)
            do update set
            name = excluded.name;
            """)
    Optional<UUID> upsert(BinaryCollectionEntity collection);

    @Query("select * from binary_collection where id = :id")
    Optional<BinaryCollectionEntity> findById(UUID id);

    @Query("delete from binary_collection where id = :id")
    boolean delete(UUID id);
}
