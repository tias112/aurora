package aurora.mag.batch;

import aurora.mag.resourcemanager.ResourceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class MagCSVReader implements ResourceAwareItemReaderItemStream<MagRecord> {
    List<MagRecord> magRec = new ArrayList<>();

    ResourceManager resourceManager;
    private Resource resource = null;
    private int count;

    public MagCSVReader( ResourceManager resourceManager) throws IOException {
        this.resourceManager = resourceManager;
    }

    private void parseMagCSV(File fileRegions) {

        log.debug("parseRegionsCSV from {}", fileRegions.getAbsolutePath());
        if (fileRegions.exists()) {
            try {
                LineIterator it = FileUtils.lineIterator(fileRegions, StandardCharsets.UTF_8.name());
                while (it.hasNext()) {
                    String[] parts = it.next().split(" {2}");
                    if (parts.length > 1) {
                        String[] components = parts[1].split(" ");
                        MagRecord magRecord = new MagRecord();
                        magRecord.setTimestamp(getTimestampStr(parts[0]));

                        magRecord.setXcomponent(Float.parseFloat(components[0].trim()));
                        magRecord.setYcomponent(Float.parseFloat(components[1].trim()));
                        if (components.length>2) {
                            magRecord.setZcomponent(Float.parseFloat(components[2].trim()));
                        }
                        if ( isDateBeforeToday(magRecord.getTimestamp()) && isDateAfterLastTimestamp(magRecord.getTimestamp())) {
                            magRec.add(magRecord);
                        }
                    }
                }
               log.info("delay : {}", Duration.between(resourceManager.getLastTimestamp(), resourceManager.getCurrentTimeInUTC()).getSeconds());
            } catch (IOException e) {
                throw new ItemStreamException(e);
            }
        } else {
            throw new ItemStreamException("File doesn't exist [" + fileRegions.getAbsolutePath() + "]");
        }
        log.info("read {} new records", magRec.size());
    }

    private String getTimestampStr(String timestamp) {
        if (resourceManager.simulateAsToday() && timestamp.length()>11) {
            LocalDateTime local = resourceManager.getCurrentTimeInUTC();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return formatter.format(local).substring(0,8) + timestamp.substring(8);
        }
        return timestamp;
    }

    private boolean isDateBeforeToday(String timestamp) {
        //skip partial line
        if (timestamp==null) {
            return true;
        }
         if (timestamp.length() < 14 ) {
            return false;
        }
        if (resourceManager.simulateAsToday()) {

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime date = LocalDateTime.parse(timestamp, formatter);

            return date.isBefore(resourceManager.getCurrentTimeInUTC());
        }
        return true;
    }
    
    private boolean isDateAfterLastTimestamp(String timestamp) {
        //skip partial line
        if (timestamp.length() < 14) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime date = LocalDateTime.parse(timestamp, formatter);
        if (resourceManager.getLastTimestamp() == null) {
            resourceManager.setLastTimestamp(date);
            return true;
        } else if (resourceManager.isLocal() || date.isAfter(resourceManager.getLastTimestamp())) {
            resourceManager.setLastTimestamp(date);
            return true;
        }
        return false;
    }

    @Override
    public MagRecord read() {
        MagRecord magRecord = null;
        if (count < magRec.size()) {
            magRecord = magRec.get(count);
            count++;
        }
        return magRecord;
    }

    @Override
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        if (resource.exists()) {
            try {
                magRec.clear();
                count=0;
                parseMagCSV(resource.getFile());
            }catch (Exception e) {
                log.error("error reading resource", e);
            }
        } else {
            log.warn("resource doesn't exist");
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

    }

    @Override
    public void close() throws ItemStreamException {
        this.resource = null;
        magRec.clear();
    }
}
