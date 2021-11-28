package aurora.mag.controller;

import aurora.mag.scheduler.JobConstants;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/")
public class AuroraController {
    @Autowired
    JobLauncher jobLauncher;
    @Autowired
    Job importMagneticJob;

    @GetMapping("/start")
    public String getMag(@RequestParam(required = false) String date) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong(JobConstants.TIME_JOB_PARAMETER, System.currentTimeMillis())
                .addString(JobConstants.IMPORT_DATE, (date != null && date.length() > 0) ? date : "")
                .toJobParameters();
        JobExecution jobExecution = jobLauncher.run(importMagneticJob, jobParameters);
        return "COMPLETE";
    }


}
