package aurora.mag.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "import")
@Data
public class ImportProperties {
    private final Scheduler scheduler = new Scheduler();
    @Value("${import.aurora.temporary_directory}")
    public String temporaryDirectory;
    @Value("${import.aurora.import_data_from}")
    public String importDataFrom;
    @Value("${import.aurora.import_data_from_timestamp}")
    public String importDataFromTimestamp;
    @Value("${import.aurora.import_data_by_date}")
    public String importDataByDate;
    @Value("${import.aurora.local_data_dir}")
    public String localDataDir;
    @Value("${import.aurora.http_data_url}")
    public String httpDataUrl;
    @Value("${import.chunk-size}")
    private Integer chunkSize;
    @Value("${import.retry-count}")
    private Integer retryCount;
    @Value("${import.aurora.cron_rt_load}")
    private String cronRtLoad;
    @Value("${import.aurora.cron_clear_temporary_directory}")
    private String cronClearTemporaryDirectory;

    //TODO
    @Data
    public static class Scheduler {
    }
}
