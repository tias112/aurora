package aurora.mag.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ProcessedItemsCountListener implements ChunkListener {

    private long startTime;

    @Override
    public void beforeChunk(ChunkContext context) {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void afterChunk(ChunkContext context) {
        log.info("{}[{}]: Processed {} items, takes {} ms",
                context.getStepContext().getJobName(),
                context.getStepContext().getStepName(),
                context.getStepContext().getStepExecution().getWriteCount(),
                System.currentTimeMillis() - startTime);
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        // Do nothing afterChunkError
    }
}
