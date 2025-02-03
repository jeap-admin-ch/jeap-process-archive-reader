package ch.admin.bit.jeap.processarchive.reader.objectstorage;

import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Map;
import java.util.Optional;

public class DecryptingStorageObjectRepository extends S3StorageObjectRepository {

    private static final String METADATA_KEY_IS_ENCRYPTED = "is_encrypted";

    private final KeyReferenceCryptoService cryptoService;

    public DecryptingStorageObjectRepository(S3Client s3Client, KeyReferenceCryptoService cryptoService) {
        super(s3Client);
        this.cryptoService = cryptoService;
    }

    @Override
    public StorageObject getObject(String bucketName, String objectKey, Optional<String> objectVersionId) {
        StorageObject storageObject = super.getObject(bucketName, objectKey, objectVersionId);

        if (isObjectEncrypted(storageObject.metadata())) {
            return new StorageObject(storageObject.key(), cryptoService.decrypt(storageObject.data()), storageObject.metadata());
        }
        return storageObject;
    }

    private static boolean isObjectEncrypted(Map<String, String> metadata) {
        return Boolean.parseBoolean(metadata.getOrDefault(METADATA_KEY_IS_ENCRYPTED, "false"));
    }
}
