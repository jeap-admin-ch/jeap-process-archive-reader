package ch.admin.bit.jeap.processarchive.reader;

import ch.admin.bit.jeap.crypto.api.KeyReferenceCryptoService;
import ch.admin.bit.jeap.crypto.internal.core.noop.NoopKeyReferenceCryptoService;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.DecryptingStorageObjectRepository;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3StorageObjectRepository;
import foo.Person;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.io.IOException;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
@EnableAutoConfiguration
@SpringBootTest
@ContextConfiguration(classes = {ProcessArchiveReaderAutoConfiguration.class})
class ProcessArchiveReaderIT {

    @Spy
    private KeyReferenceCryptoService cryptoService = new NoopKeyReferenceCryptoService();

    @MockBean
    private S3Client s3Client;

    @Test
    void readArtifact() throws Exception {
        stubS3Responses(false);
        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(new S3StorageObjectRepository(s3Client));

        Person person = processArchiveReader.readArtifact(Person.class, "bucket", "key");

        assertThat(person.getAge()).isEqualTo(33);
    }

    @Test
    void readArtifactWithDecryption() throws Exception {
        stubS3Responses(true);
        ProcessArchiveReader decryptingReader = new ProcessArchiveReader(new DecryptingStorageObjectRepository(s3Client, cryptoService));

        Person person = decryptingReader.readArtifact(Person.class, "bucket", "key");
        assertThat(person.getAge()).isEqualTo(33);
        verify(cryptoService).decrypt(any());
    }

    private void stubS3Responses(boolean encrypted) throws IOException {
        byte[] schema = TestUtils.getStringFromResource("avro/v2/person-schema.avsc").getBytes(UTF_8);
        byte[] object = TestUtils.jsonToAvro(TestUtils.getStringFromResource("avro/v2/person-data.json"), TestUtils.getSchemaFromResource("avro/v2/person-schema.avsc"));

        ResponseBytes<GetObjectResponse> getObjectResponseSchema = mock(ResponseBytes.class);
        when(getObjectResponseSchema.asByteArray()).thenReturn(schema);

        ResponseBytes<GetObjectResponse> getObjectResponseObject = mock(ResponseBytes.class);
        when(getObjectResponseObject.asByteArray()).thenReturn(object);

        GetObjectRequest getObjectRequestSchema = GetObjectRequest.builder()
                .bucket("bucket")
                .key("schemaLocation")
                .build();

        GetObjectRequest getObjectRequestObject = GetObjectRequest.builder()
                .bucket("bucket")
                .key("key")
                .build();

        when(s3Client.getObjectAsBytes(getObjectRequestSchema)).thenReturn(getObjectResponseSchema);

        when(s3Client.getObjectAsBytes(getObjectRequestObject)).thenReturn(getObjectResponseObject);

        Map<String, String> metadataMap = encrypted ?
                Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation", "is_encrypted", "true") :
                Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation");
        Map<String, String> schemaMetadataMap = Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation");
        ;
        HeadObjectResponse metadataResponse = HeadObjectResponse.builder()
                .metadata(metadataMap)
                .build();
        HeadObjectResponse schemaMetadataResponse = HeadObjectResponse.builder()
                .metadata(schemaMetadataMap)
                .build();
        when(s3Client.headObject(any(HeadObjectRequest.class)))
                .thenReturn(metadataResponse)
                .thenReturn(metadataResponse)
                .thenReturn(schemaMetadataResponse);
    }
}
