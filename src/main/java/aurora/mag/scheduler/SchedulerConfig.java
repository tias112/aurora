package aurora.mag.scheduler;

import aurora.mag.config.ImportProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Slf4j
@Configuration
public class SchedulerConfig {

    private final String cronExpressionForClearTmpDir;
    private final String cronExpressionForBIImport;

    @Autowired
    private ApplicationContext applicationContext;

    public SchedulerConfig(ImportProperties properties) {
        cronExpressionForClearTmpDir = properties.getCronClearTemporaryDirectory();
        cronExpressionForBIImport = properties.getCronRtLoad();

        log.info("cronExpressionForClearTmpDir = {}", cronExpressionForClearTmpDir);
        log.info("cronExpressionForBIImport = {}", cronExpressionForBIImport);
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {

        StdSchedulerFactory factory = new StdSchedulerFactory();
        Scheduler scheduler = factory.getScheduler();
        scheduler.setJobFactory(springBeanJobFactory());

        scheduleJob(scheduler, cronExpressionForClearTmpDir, ClearTempDirectoryQuartzJob.class);
        scheduleJob(scheduler, cronExpressionForBIImport, ImportMagneticQuartzJob.class);

        scheduler.start();
        return scheduler;
    }

    private void scheduleJob(Scheduler scheduler, String cronExpression, Class<? extends Job> jobClass) throws SchedulerException {

        if (StringUtils.isNotEmpty(cronExpression)) {

            String name = jobClass.getSimpleName();

            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("cronTrigger_" + name)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            JobDetail jobDetail = JobBuilder.newJob().ofType(jobClass)
                    .storeDurably()
                    .withIdentity("jobDetail_" + name)
                    .build();

            scheduler.scheduleJob(jobDetail, cronTrigger);
        }
    }

    private SpringBeanJobFactory springBeanJobFactory() {
        AutoWiringSpringBeanJobFactory jobFactory = new AutoWiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }
}
