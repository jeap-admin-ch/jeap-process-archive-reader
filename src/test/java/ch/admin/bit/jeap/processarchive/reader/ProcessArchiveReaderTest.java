package ch.admin.bit.jeap.processarchive.reader;

import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3StorageObjectRepository;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.StorageObject;
import foo.Person;
import lombok.SneakyThrows;
import org.apache.avro.AvroTypeException;
import org.apache.avro.SchemaParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessArchiveReaderTest {

    @Mock
    private S3StorageObjectRepository s3StorageObjectRepository;

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testReadArtifact_writerCompatibleWithReaderSchema_objectReturned() {
        //v2 of Person is compatible with reader schema (v3)
        setupDataInBucketWithVersion("avro/v2");
        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(s3StorageObjectRepository);
        Person person = processArchiveReader.readArtifact(Person.class, "bucket", "key");

        assertThat(person).isNotNull();
        assertThat(person.getName()).hasToString("Hans");
        assertThat(person.getAge()).isEqualTo(33);
        assertThat(person.getSurnameNew()).isNull();

        ArgumentCaptor<Optional<String>> argumentCaptor = ArgumentCaptor.forClass(Optional.class);
        verify(s3StorageObjectRepository, times(1)).getObject(anyString(), anyString(), argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues().stream().filter(Optional::isPresent).findFirst()).isEmpty();
    }

    @Test
    @SneakyThrows
    void testReadArtifact_writerNotCompatibleWithReaderSchema_throwsException() {
        //v1 of Person is not compatible with reader schema (v3)
        setupDataInBucketWithVersion("avro/v1");
        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(s3StorageObjectRepository);
        ProcessArchiveReaderException e = assertThrows(ProcessArchiveReaderException.class, () -> processArchiveReader.readArtifact(Person.class, "bucket", "key"));
        assertEquals(AvroTypeException.class, e.getCause().getClass());
    }

    @Test
    @SneakyThrows
    @SuppressWarnings("unchecked")
    void testReadArtifactWithVersion_writerCompatibleWithReaderSchema_objectReturned() {
        //v2 of Person is compatible with reader schema (v3)
        setupDataInBucketWithVersion("avro/v2");
        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(s3StorageObjectRepository);
        Person person = processArchiveReader.readArtifact(Person.class, "bucket", "key", "version1");

        assertThat(person).isNotNull();
        assertThat(person.getName()).hasToString("Hans");
        assertThat(person.getAge()).isEqualTo(33);
        assertThat(person.getSurnameNew()).isNull();

        ArgumentCaptor<Optional<String>> argumentCaptor = ArgumentCaptor.forClass(Optional.class);
        verify(s3StorageObjectRepository, times(1)).getObject(anyString(), anyString(), argumentCaptor.capture());

        Optional<Optional<String>> optionalVersion = argumentCaptor.getAllValues().stream().filter(Optional::isPresent).findFirst();
        assertThat(optionalVersion).isPresent();
        assertThat(optionalVersion.get()).contains("version1");
    }

    @Test
    @SneakyThrows
    void testReadArtifact_writerSchemaNotFound_throwsException() {
        Map<String, String> metadata = Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation");
        when(s3StorageObjectRepository.getObject(eq("bucket"), eq("key"), any()))
                .thenReturn(new StorageObject("key", null, metadata));
        when(s3StorageObjectRepository.getObjectAsBytes(eq("bucket"), eq("schemaLocation")))
                .thenThrow(NoSuchKeyException.class);

        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(s3StorageObjectRepository);
        assertThatThrownBy(() -> processArchiveReader.readArtifact(Person.class, "bucket", "key"))
                .isInstanceOf(ProcessArchiveReaderException.class)
                .hasMessageContaining("Error reading the writer schema with key schemaLocation from bucket bucket");
    }

    @Test
    @SneakyThrows
    void testReadArtifact_writerSchemaNotParseable_throwsException() {
        Map<String, String> metadata = Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation");
        when(s3StorageObjectRepository.getObject(eq("bucket"), eq("key"), any()))
                .thenReturn(new StorageObject("key", null, metadata));
        when(s3StorageObjectRepository.getObjectAsBytes(eq("bucket"), eq("schemaLocation")))
                .thenReturn("not_parseable".getBytes(UTF_8));
        ProcessArchiveReader processArchiveReader = new ProcessArchiveReader(s3StorageObjectRepository);

        assertThatThrownBy(() -> processArchiveReader.readArtifact(Person.class, "bucket", "key"))
                .isInstanceOf(ProcessArchiveReaderException.class)
                .hasCauseInstanceOf(SchemaParseException.class);
    }

    @SneakyThrows
    private void setupDataInBucketWithVersion(String version) {
        // Return schema
        byte[] schemaBytes = TestUtils.getStringFromResource(version + "/person-schema.avsc").getBytes(UTF_8);
        when(s3StorageObjectRepository.getObjectAsBytes(eq("bucket"), eq("schemaLocation")))
                .thenReturn(schemaBytes);
        // Return schema location
        Map<String, String> metadata = Map.of(ProcessArchiveReader.SCHEMA_FILE_KEY, "schemaLocation");
        // Return Data according schema
        byte[] dataBytes = TestUtils.jsonToAvro(TestUtils.getStringFromResource(version + "/person-data.json"), TestUtils.getSchemaFromResource(version + "/person-schema.avsc"));
        when(s3StorageObjectRepository.getObject(eq("bucket"), eq("key"), any()))
                .thenReturn(new StorageObject("key", dataBytes, metadata));
    }
}
