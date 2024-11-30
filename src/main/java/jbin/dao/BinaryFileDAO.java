package jbin.dao;

import jbin.entity.BinaryFileEntity;
import jbin.orm.Query;
import jbin.orm.Table;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Table(name = "binary_files", createTable = """
        create table if not exists binary_files
        (
            id uuid primary key not null ,
            name varchar(10000) not null ,
            creation_date timestamp not null ,
            content_type varchar(255)
        );
        """)
public interface BinaryFileDAO {
    @Query("""
            insert into binary_files (id, name, creation_date, content_type) values (:id, :name, :creationDate, :contentType)
            """)
    Optional<UUID> insert(BinaryFileEntity file);

    @Query("delete from binary_files where id = :id")
    boolean delete(UUID id);

    @Query("select * from binary_files where id = :id")
    Optional<BinaryFileEntity> findById(UUID id);

    @Query("select * from binary_files")
    List<BinaryFileEntity> getAll();

    @Query("delete from binary_files where id not in (select file_id from file_collection) returning *")
    List<BinaryFileEntity> deleteOrphans();
}
