package ch.admin.bit.jeap.processarchive.reader;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.AvroTypeException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Slf4j
public class ProcessArchiveReaderException extends RuntimeException {

    private ProcessArchiveReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    private ProcessArchiveReaderException(String message) {
        super(message);
    }

    public static ProcessArchiveReaderException ioException(String archiveBindingType, String bucket, String key, IOException e) {
        String msg = String.format("Error reading object with key %s to datatype %s from bucket %s", key, archiveBindingType, bucket);
        log.error(msg, e);
        return new ProcessArchiveReaderException(msg, e);
    }

    public static ProcessArchiveReaderException readException(String archiveBindingType, String bucket, String key, AvroTypeException e) {
        String msg = String.format("Error converting object with key %s to datatype %s from bucket %s", key, archiveBindingType, bucket);
        log.error(msg, e);
        return new ProcessArchiveReaderException(msg, e);
    }

    public static ProcessArchiveReaderException writerSchemaNotReadableException(String bucket, String key, S3Exception e) {
        String msg = String.format("Error reading the writer schema with key %s from bucket %s : %s", key, bucket, e.getMessage());
        log.error(msg);
        return new ProcessArchiveReaderException(msg, e);
    }
    public static ProcessArchiveReaderException writerSchemaParseException(String bucket, String key, Exception e) {
        String msg = String.format("Error parsing the writer schema with key %s from bucket %s", key, bucket);
        log.error(msg, e);
        return new ProcessArchiveReaderException(msg, e);
    }
}
