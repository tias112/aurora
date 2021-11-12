package aurora.mag.repo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

@Slf4j
@Repository
public class BatchJobsDao {

    private static final String FIND_JOB_EXECUTIONS_QUERY = "SELECT JOB_EXECUTION_ID from BATCH_JOB_EXECUTION where START_TIME > ? AND START_TIME < ?order by START_TIME desc offset ? rows fetch first ? rows only";

    private static final String FIND_JOB_EXECUTIONS_BY_STATUS_QUERY = "SELECT JOB_EXECUTION_ID from BATCH_JOB_EXECUTION where START_TIME > ? AND START_TIME < ? AND STATUS = ? order by START_TIME desc offset ? rows fetch first ? rows only";

    private static final String SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT = "DELETE FROM BATCH_STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID IN (SELECT STEP_EXECUTION_ID FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  BATCH_JOB_EXECUTION where CREATE_TIME < ?))";

    private static final String SQL_DELETE_BATCH_STEP_EXECUTION = "DELETE FROM BATCH_STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION where CREATE_TIME < ?)";

    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT = "DELETE FROM BATCH_JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  BATCH_JOB_EXECUTION where CREATE_TIME < ?)";

    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS = "DELETE FROM BATCH_JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM BATCH_JOB_EXECUTION where CREATE_TIME < ?)";

    private static final String SQL_DELETE_BATCH_JOB_EXECUTION = "DELETE FROM BATCH_JOB_EXECUTION where CREATE_TIME < ?";

    private static final String SQL_DELETE_BATCH_JOB_INSTANCE = "DELETE FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID NOT IN (SELECT JOB_INSTANCE_ID FROM BATCH_JOB_EXECUTION)";

    private static final String JOB_EXECUTION_ID_FIELD = "JOB_EXECUTION_ID";

    private final JdbcTemplate jdbcTemplate;

    public BatchJobsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Stream<Long> findJobExecutionsIdsByStatus(@Nullable Date fromDate, @Nullable Date toDate, BatchStatus status,
                                                     int offset, int limit) {
        if (fromDate == null) {
            fromDate = new Date(0);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        return jdbcTemplate.query(FIND_JOB_EXECUTIONS_BY_STATUS_QUERY,
                new Object[]{new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()),
                        status.toString(), offset, limit},
                (rs, rowNum) -> rs.getLong(JOB_EXECUTION_ID_FIELD)).stream();
    }

    public Stream<Long> findJobExecutionsIds(@Nullable Date fromDate, @Nullable Date toDate, int offset, int limit) {
        if (fromDate == null) {
            fromDate = new Date(0);
        }
        if (toDate == null) {
            toDate = new Date();
        }
        return jdbcTemplate.query(FIND_JOB_EXECUTIONS_QUERY,
                new Object[]{new Timestamp(fromDate.getTime()), new Timestamp(toDate.getTime()), offset, limit},
                (rs, rowNum) -> rs.getLong(JOB_EXECUTION_ID_FIELD)).stream();
    }

    public void clearHistoryBefore(Date beforeDate) {
        log.debug("Clear spring batch history before {}", new SimpleDateFormat("yyyy-MM-dd").format(beforeDate));

        int rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT, beforeDate);
        log.debug("Deleted rows number from the BATCH_STEP_EXECUTION_CONTEXT table: {}", rowCount);

        rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_STEP_EXECUTION, beforeDate);
        log.debug("Deleted rows number from the BATCH_STEP_EXECUTION table: {}", rowCount);

        rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT, beforeDate);
        log.debug("Deleted rows number from the BATCH_JOB_EXECUTION_CONTEXT table: {}", rowCount);

        rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS, beforeDate);
        log.debug("Deleted rows number from the BATCH_JOB_EXECUTION_PARAMS table: {}", rowCount);

        rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_JOB_EXECUTION, beforeDate);
        log.debug("Deleted rows number from the BATCH_JOB_EXECUTION table: {}", rowCount);

        rowCount = jdbcTemplate.update(SQL_DELETE_BATCH_JOB_INSTANCE);
        log.debug("Deleted rows number from the BATCH_JOB_INSTANCE table: {}", rowCount);
    }
}
