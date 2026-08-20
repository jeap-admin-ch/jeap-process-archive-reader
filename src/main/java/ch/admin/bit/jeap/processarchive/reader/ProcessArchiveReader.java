package ch.admin.bit.jeap.processarchive.reader;

import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3StorageObjectRepository;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.StorageObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ProcessArchiveReader {

    public static final String SCHEMA_FILE_KEY = "schema-file-key";

    private final S3StorageObjectRepository s3StorageObjectRepository;

    public <T> T readArtifact(Class<T> archiveBindingType, String bucket, String key) {
        return readArtifact(archiveBindingType, bucket, key, null);
    }

    public <T> T readArtifact(Class<T> archiveBindingType, String bucket, String key, String version) {
        log.debug("Read artifact of type {} from bucket {} with key {}", archiveBindingType.getName(), bucket, key);
        Optional<String> versionOptional = Optional.ofNullable(version);
        StorageObject storageObject = s3StorageObjectRepository.getObject(bucket, removeLeadingSlash(key), versionOptional);
        DatumReader<T> datumReader = new SpecificDatumReader<>();
        datumReader.setSchema(getWriterSchema(bucket, storageObject));
        Decoder decoder = DecoderFactory.get().binaryDecoder(storageObject.data(), null);

        try {
            return datumReader.read(null, decoder);
        } catch (AvroTypeException e) {
            throw ProcessArchiveReaderException.readException(archiveBindingType.getName(), bucket, key, e);
        } catch (IOException e) {
            throw ProcessArchiveReaderException.ioException(archiveBindingType.getName(), bucket, key, e);
        }
    }

    private Schema getWriterSchema(String bucket, StorageObject storageObject) {
        String schemaFileKey = storageObject.metadata().get(SCHEMA_FILE_KEY);
        try {
            byte[] schema = s3StorageObjectRepository.getObjectAsBytes(bucket, schemaFileKey);
            return parseSchema(bucket, schema, schemaFileKey);
        } catch (S3Exception exception) {
            throw ProcessArchiveReaderException.writerSchemaNotReadableException(bucket, schemaFileKey, exception);
        }
    }

    private static Schema parseSchema(String bucket, byte[] schema, String schemaFileKey) {
        try {
            return new Schema.Parser().parse(new String(schema));
        } catch (Exception e) {
            throw ProcessArchiveReaderException.writerSchemaParseException(bucket, schemaFileKey, e);
        }
    }

    private String removeLeadingSlash(String pathSegments) {
        return pathSegments.startsWith("/") ? pathSegments.substring(1) : pathSegments;
    }
}
