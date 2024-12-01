package jbin.data;

import io.minio.*;
import jbin.domain.FileBucket;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
public class S3FileBucket implements FileBucket {
    private final MinioClient client;
    private final String bucket;

    public S3FileBucket(String endpoint, String accessKey, String secretKey, String bucket) {
        client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
        this.bucket = bucket;
        try {
            var found = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public Optional<InputStream> get(String id) {
        try {
            var response = client.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build()
            );
            return Optional.of(response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean put(String id, InputStream inputStream) {
        try {
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .stream(inputStream, inputStream.available(), -1)
                            .build()
            );
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean delete(String id) {
        try {
            client.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(id)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
