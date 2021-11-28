package aurora.mag.batch;

import aurora.mag.batch.log.MagItemWriter;
import aurora.mag.config.ImportProperties;
import aurora.mag.resourcemanager.HttpRemoteResourceManager;
import aurora.mag.resourcemanager.LocalResourceManager;
import aurora.mag.resourcemanager.ResourceManager;
import aurora.mag.resourcemanager.TemporaryFileStorageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

// tag::setup[]
@Slf4j
@Configuration
@EnableBatchProcessing
public class MagneticConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;


    @Autowired
    public TemporaryFileStorageManager temporaryFileStorageManager;

    @Autowired
    public ImportProperties importProperties;


    @Bean
    public ResourceManager resourceManager() throws ParseException {

        String dataFrom = importProperties.importDataFrom.toLowerCase();

        String fromLocal = "local";
        String fromHttp = "http";

        if (dataFrom.equals("local")) {
            log.info("Get data from local");
            return new LocalResourceManager(importProperties);
        }
        if (dataFrom.equals("http")) {
            log.info("Get data from http");
            return new HttpRemoteResourceManager(importProperties, temporaryFileStorageManager);
        } else {
            throw new IllegalArgumentException(
                    "not supported dataFrom: $dataFrom. available: [" + fromLocal + ", " + fromHttp + "]"
            );
        }
    }


    @Bean
    public Job importMagneticJob(ProcessedItemsCountListener listener,
                                 Step readMagnetic,
                                 Step noopStep
    ) {

        return jobBuilderFactory.get("importMagneticJob")
                .incrementer(new RunIdIncrementer())
                .start(readMagnetic)
                //.start(noopStep)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<MagRecord> magItemReader(ResourceManager resourceManager) throws IOException {
        List<Resource> magneticFiles = resourceManager.getMagneticFiles();
        log.info("Found mag files to import: {}", magneticFiles.size());
        return new MultiResourceItemReaderBuilder<MagRecord>()
                .resources(magneticFiles.toArray(new Resource[magneticFiles.size()]))
                .delegate(new MagCSVReader(resourceManager))
                .name("magCsvReader")
                .build();
        //return new MagCSVReader(resourceManager.getMagneticFile(), resourceManager);
    }

    @Bean
    @StepScope
    public ItemWriter<MagRecord> magItemWriter(
    ) {
        return new MagItemWriter();
    }

    @Bean
    public Step readMagnetic(ProcessedItemsCountListener listener,
                             ItemReader<MagRecord> magItemReader,
                             ItemWriter<MagRecord> magItemWriter,
                             ImportProperties importProperties
    ) {
        return stepBuilderFactory.get("readMagnetic")
                .<MagRecord, MagRecord>chunk(importProperties.getChunkSize())
                .reader(magItemReader)
                .writer(magItemWriter)
                .listener(listener)
                .build();
    }


}