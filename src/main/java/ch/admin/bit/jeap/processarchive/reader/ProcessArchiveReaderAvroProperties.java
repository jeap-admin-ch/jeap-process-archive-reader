package ch.admin.bit.jeap.processarchive.reader;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ToString
@ConfigurationProperties("jeap.process-archive.reader.avro")
public class ProcessArchiveReaderAvroProperties {

    /**
     * Packages trusted for Avro deserialization in addition to the default package {@code ch.admin}.
     */
    private List<String> additionalTrustedPackages = List.of();

    /**
     * Classes trusted for Avro deserialization in addition to the default package {@code ch.admin}.
     */
    private List<String> additionalTrustedClasses = List.of();
}
