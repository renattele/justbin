package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.UUID;

@Table(name = "binary_collection", createTable = """
        create table binary_collection
        (
            id uuid primary key,
            name varchar(10000)
        );
        """)
public interface BinaryCollectionRepository {
    @Query("""
            insert into binary_collection (id, name) values (?, ?)
            on conflict (id)
            do update set
            name = excluded.name;
            """)
    UUID upsert(BinaryCollection collection);

    @Query("select * from binary_collection where id = ?")
    BinaryCollection findById(UUID id);

    @Query("delete from binary_collection where id = ?")
    boolean delete(UUID id);
}