package aurora.mag.resourcemanager;

import aurora.mag.config.ImportProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class TemporaryFileStorageManager {
    private final String dataTempDirectory;

    @Autowired
    public TemporaryFileStorageManager(ImportProperties properties) {
        String temporaryDirectory = properties.temporaryDirectory;

        if (!(temporaryDirectory.endsWith("/") || temporaryDirectory.endsWith("\\"))) {
            temporaryDirectory = temporaryDirectory.concat(File.separator);
        }

        dataTempDirectory = temporaryDirectory;

        log.debug("dataTempDirectory = {}", dataTempDirectory);
    }

    public String getDataTempDirectory() {
        return dataTempDirectory;
    }

    public boolean isFileExists(String fileName) {
        return Paths.get(dataTempDirectory + fileName).toFile().exists();
    }

    public boolean isFileNotExists(String fileName) {
        return !isFileExists(fileName);
    }

    public Resource getResource(String fileName) {

        if (isFileNotExists(fileName)) {
            String exMessage = "File " + fileName + " no found in temp directory";
            log.error(exMessage);
            throw new ResourceException(exMessage);
        }

        String filePath = dataTempDirectory + fileName;
        return new FileSystemResource(filePath);
    }

    public File newTempFile(String fileName) {
        return new File(dataTempDirectory, fileName);
    }

    public void clearAll() throws IOException {
        FileUtils.cleanDirectory(new File(getDataTempDirectory()));
    }

    public void clearByPrefix(String prefix) {
        Collection<File> files = FileUtils.listFiles(new File(getDataTempDirectory()), FileFilterUtils.prefixFileFilter(prefix), null);
        log.debug("Clear by prefix = {} files = {}", prefix, files);
        for (File file : files) {
            try {
                log.debug("Delete file {}", file.getAbsolutePath());
                FileUtils.forceDelete(file);
            } catch (Exception e) {
                log.error(String.format("Error delete file %s", file.getAbsolutePath()), e);
            }
        }
    }

    public void clearByPrefixes(List<String> prefixes) {
        if (prefixes != null && !prefixes.isEmpty()) {
            prefixes.forEach(this::clearByPrefix);
        }
    }
}
