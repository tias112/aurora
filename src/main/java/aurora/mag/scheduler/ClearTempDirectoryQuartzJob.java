package aurora.mag.scheduler;

import aurora.mag.resourcemanager.TemporaryFileStorageManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class ClearTempDirectoryQuartzJob implements Job {

    @Autowired
    private TemporaryFileStorageManager temporaryFileStorageManager;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info(">> ClearTempDirectoryQuartzJob start: {}", start);
        try {
            temporaryFileStorageManager.clearAll();
        } catch (Exception e) {
            log.error("Error during ClearTempDirectoryQuartzJob executing: ", e);
        } finally {
            LocalDateTime end = LocalDateTime.now();
            log.info("<< ClearTempDirectoryQuartzJob end: {}, duration: {}", end, Duration.between(start, end));
        }
    }
}