package ch.admin.bit.jeap.processarchive.reader;

import ch.admin.bit.jeap.messaging.avro.security.AvroClassSecurity;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3ObjectStorageConfiguration;
import ch.admin.bit.jeap.processarchive.reader.objectstorage.S3StorageObjectRepository;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.ArrayList;
import java.util.List;

@AutoConfigureAfter(S3ObjectStorageConfiguration.class)
@EnableConfigurationProperties(ProcessArchiveReaderAvroProperties.class)
public class ProcessArchiveReaderAutoConfiguration {

    public ProcessArchiveReaderAutoConfiguration(ProcessArchiveReaderAvroProperties avroProperties) {
        // Install the whitelist before any archived object can be read, i.e. before the reader bean is created
        installAvroClassSecurity(avroProperties);
    }

    @Bean
    @ConditionalOnBean(S3Client.class)
    public ProcessArchiveReader archiveReader(S3Client s3Client){
        return new ProcessArchiveReader(new S3StorageObjectRepository(s3Client));
    }

    /**
     * Avro deserializes generated classes only if they are whitelisted. Archived objects usually live in the default
     * package {@code ch.admin}, services reading objects of other packages have to add them to the whitelist.
     */
    private static void installAvroClassSecurity(ProcessArchiveReaderAvroProperties avroProperties) {
        if (avroProperties.getAdditionalTrustedPackages().isEmpty() && avroProperties.getAdditionalTrustedClasses().isEmpty()) {
            // Do not replace a whitelist that has already been installed, i.e. the one of jeap-messaging
            AvroClassSecurity.installDefaultIfMissing();
            return;
        }
        List<String> trustedPackages = new ArrayList<>(avroProperties.getAdditionalTrustedPackages());
        trustedPackages.add(AvroClassSecurity.DEFAULT_TRUSTED_PACKAGE);
        AvroClassSecurity.install(trustedPackages, avroProperties.getAdditionalTrustedClasses());
    }

}
