package jbin.domain;

import jbin.orm.Query;
import jbin.orm.Table;

import java.util.List;
import java.util.UUID;

@Table(name = "file_collection", createTable = """
        create table file_collection(
            id uuid primary key,
            file_id uuid not null,
            collection_id uuid not null,
            foreign key (file_id) references binary_files(id),
            foreign key (collection_id) references binary_collection(id)
        )
        """)
public interface FileCollectionRepository {
    @Query("""
            insert into file_collection (id, file_id, collection_id) values (?, ?, ?)
            on conflict (id)
            do update set
            file_id = excluded.file_id,
            collection_id = excluded.collection_id
            """)
    UUID upsert(FileCollection fileCollection);

    @Query("select * from file_collection where id = ?")
    FileCollection findById(UUID id);

    @Query("select * from file_collection where collection_id = ?")
    List<FileCollection> getAllByCollectionId(UUID id);

    @Query("delete from file_collection where id = ?")
    boolean deleteById(UUID id);

    @Query("delete from file_collection where file_id = ?")
    boolean deleteByFileId(UUID id);

    @Query("delete from file_collection where file_id = ? and collection_id = ?")
    boolean deleteByFileAndCollectionId(UUID fileId, UUID collectionId);

    @Query("delete from file_collection where collection_id = ?")
    boolean deleteAllByCollectionId(UUID id);
}