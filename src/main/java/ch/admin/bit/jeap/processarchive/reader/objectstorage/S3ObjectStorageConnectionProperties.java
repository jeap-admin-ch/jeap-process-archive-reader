package ch.admin.bit.jeap.processarchive.reader.objectstorage;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.regions.Region;

@Getter
@ToString
@ConfigurationProperties("jeap.process-archive.reader.connection")
public class S3ObjectStorageConnectionProperties {

    @Setter
    private String accessUrl;

    private Region region = Region.AWS_GLOBAL;

    @Setter
    // excluded from toString for security reasons
    @ToString.Exclude
    private String accessKey;

    @Setter
    // excluded from toString for security reasons
    @ToString.Exclude
    private String secretKey;

    public void setRegion(String region) {
        this.region = Region.of(region);
    }
}
