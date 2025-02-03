package ch.admin.bit.jeap.processarchive.reader.objectstorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class S3StorageObjectRepository {

    private final S3Client s3Client;

    public byte[] getObjectAsBytes(String bucketName, String objectKey) {
        GetObjectRequest request = createGetObjectRequest(bucketName, objectKey, Optional.empty());
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    public StorageObject getObject(String bucketName, String objectKey, Optional<String> objectVersionId) {
        GetObjectRequest request = createGetObjectRequest(bucketName, objectKey, objectVersionId);
        byte[] data = s3Client.getObjectAsBytes(request).asByteArray();
        Map<String, String> metadata = getMetadata(bucketName, objectKey, objectVersionId);
        return new StorageObject(objectKey, data, metadata);
    }

    private GetObjectRequest createGetObjectRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return GetObjectRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting object with key '{}' from bucket '{}'.", objectKey, bucketName);
            return GetObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }

    private Map<String, String> getMetadata(String bucketName, String objectKey, Optional<String> objectVersionId) {
        HeadObjectRequest request = createGetObjectMetadataRequest(bucketName, objectKey, objectVersionId);
        return s3Client.headObject(request).metadata();
    }

    private HeadObjectRequest createGetObjectMetadataRequest(String bucketName, String objectKey, Optional<String> objectVersionId) {
        if (objectVersionId.isPresent()) {
            log.debug("Getting metadata for object with key '{}' and version id '{}' from bucket '{}'.", objectKey, objectVersionId.get(), bucketName);
            return HeadObjectRequest.builder().bucket(bucketName).key(objectKey).versionId(objectVersionId.get()).build();
        } else {
            log.debug("Getting metadata for object with key '{}' from bucket '{}'.", objectKey, bucketName);
            return HeadObjectRequest.builder().bucket(bucketName).key(objectKey).build();
        }
    }
}
