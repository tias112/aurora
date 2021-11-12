package aurora.mag.scheduler;

import aurora.mag.repo.BatchJobsDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobsHistoryManager {
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final BatchJobsDao batchJobsDao;

    public JobsHistoryManager(JobExplorer jobExplorer,
                              JobOperator jobOperator, BatchJobsDao batchJobsDao) {
        this.jobExplorer = jobExplorer;
        this.jobOperator = jobOperator;
        this.batchJobsDao = batchJobsDao;
    }


    public List<JobExecution> getAllRunningJobs() {
        return getExecutionsWithStatus(BatchStatus.STARTED, null, null, Integer.MAX_VALUE, 0).stream()
                .filter(indexingJobExecution -> (indexingJobExecution.getEndTime() == null))
                .collect(Collectors.toList());
    }

    private List<JobExecution> getExecutionsWithStatus(
            BatchStatus status,
            @Nullable Date fromDate,
            @Nullable Date toDate,
            int limit,
            int offset) {

        return batchJobsDao.findJobExecutionsIdsByStatus(fromDate, toDate, status, offset, limit)
                .map(jobExplorer::getJobExecution)
                .map(JobExecution::new)
                .collect(Collectors.toList());
    }



    public boolean existRunningJob() {
        List<JobExecution> allRunningJobs = getAllRunningJobs();
        return (allRunningJobs != null) && (!allRunningJobs.isEmpty());
    }


}
