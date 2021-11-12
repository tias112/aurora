package aurora.mag.scheduler;

import aurora.mag.resourcemanager.AnotherJobRunningException;
import aurora.mag.resourcemanager.TemporaryFileStorageManager;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class ImportMagneticQuartzJob implements Job {

    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    JobsHistoryManager jobsHistoryManager;
    @Autowired
    org.springframework.batch.core.Job importMagneticJob;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime start = LocalDateTime.now();
        log.info(">> ImportMagneticQuartzJob start: {}", start);
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("jobTimestamp", System.currentTimeMillis())
                    .toJobParameters();
            checkRunningJobs();
            JobExecution jobExecution = jobLauncher.run(importMagneticJob, jobParameters);

        } catch (Exception e) {
            log.error("Error during ImportMagneticQuartzJob executing: ", e);
        } finally {
            LocalDateTime end = LocalDateTime.now();
            log.info("<< ImportMagneticQuartzJob end: {}, duration: {}", end, Duration.between(start, end));
        }
    }

    private void checkRunningJobs() {
        if (jobsHistoryManager.existRunningJob()) {
            throw new AnotherJobRunningException("Another job is running. ");
        }
    }

}