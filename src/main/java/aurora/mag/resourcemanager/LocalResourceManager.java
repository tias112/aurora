package aurora.mag.resourcemanager;

import aurora.mag.config.ImportProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class LocalResourceManager extends ResourceManager {

    private final String dataDir;

    public LocalResourceManager(ImportProperties importProperties) throws ParseException {
        super(importProperties);

        dataDir = importProperties.getLocalDataDir();
        log.debug("dataDir = {}", dataDir);
    }

    @Override
    public List<String> getResourceNamesByPrefix(String prefix) {
        try {
            return getFiles().stream()
                    .filter(f -> f.getName().startsWith(prefix))
                    .map(File::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error get resource names  by prefix = {} ", prefix);
        }

        return Collections.emptyList();
    }

    @Override
    public Resource getResourceByFullName(String name) {
        File file = new File(StringUtils.join(getCurrentPath(), name));
        return new FileSystemResource(file);
    }

    private String getCurrentPath() {

        return StringUtils.join(dataDir, File.separator);
    }

    private Collection<File> getFiles() {
        return FileUtils.listFiles(new File(getCurrentPath()), null, false);
    }
}

