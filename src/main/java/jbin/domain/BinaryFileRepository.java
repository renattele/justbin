package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.List;
import java.util.UUID;

@Table(name = "binary_files", createTable = """
        create table binary_files
        (
            id uuid primary key not null ,
            collection_id uuid not null ,
            name varchar(10000) not null ,
            creation_date timestamp not null ,
            last_updated_date timestamp not null,
            readonly boolean,
            content_type varchar(255),
            foreign key (collection_id) references binary_collection(id)
        );
        """)
public interface BinaryFileRepository {
    @Query("""
            insert into binary_files (id, collection_id, name, creation_date, last_updated_date, readonly, content_type) values (?, ?, ?, ?, ?, ?, ?)
            on conflict (id)
            do update set
            name = excluded.name,
            last_updated_date = excluded.last_updated_date;
            """)
    UUID upsert(BinaryFile file);

    @Query("delete from binary_files where id = ?")
    boolean delete(UUID id);

    @Query("select * from binary_files where id = ?")
    BinaryFile findById(UUID id);

    @Query("select * from binary_files where collection_id = ?")
    List<BinaryFile> findAllByCollectionId(UUID collectionId);
}
