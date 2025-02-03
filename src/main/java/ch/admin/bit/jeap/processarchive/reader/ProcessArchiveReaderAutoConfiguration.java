package ch.admin.bit.jeap.processarchive.reader;

import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3ObjectStorageConfiguration;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3StorageObjectRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

@AutoConfigureAfter(S3ObjectStorageConfiguration.class)
public class ProcessArchiveReaderAutoConfiguration {

    @Bean
    @ConditionalOnBean(S3Client.class)
    public ProcessArchiveReader archiveReader(S3Client s3Client){
        return new ProcessArchiveReader(new S3StorageObjectRepository(s3Client));
    }

}
