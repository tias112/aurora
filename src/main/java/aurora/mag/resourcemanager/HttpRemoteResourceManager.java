package aurora.mag.resourcemanager;

import aurora.mag.config.ImportProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HttpRemoteResourceManager extends ResourceManager {

    private final String magUrl;
    private int retryCount = 1;

    private final TemporaryFileStorageManager tempFileStorageManager;
    private final ConcurrentHashMap<String, List<String>> cacheFileNamesForUrl = new ConcurrentHashMap<>();

    public HttpRemoteResourceManager(ImportProperties importProperties, TemporaryFileStorageManager temporaryFileStorageManager) throws ParseException {
        super(importProperties);
        this.retryCount = importProperties.getRetryCount();
        tempFileStorageManager = temporaryFileStorageManager;

        magUrl = importProperties.getHttpDataUrl();
        log.debug("spUrl = {}, atgUrl = {}", magUrl);

    }

    @Override
    public List<String> getResourceNamesByPrefix(String prefix) {
        log.info("getResourceNamesByPrefix  by prefix = {}", prefix);
        return List.of(prefix);
    }

    @Override
    public String getMagneticFileLocation(Date executionJobDate) {
        return RT_FILENAME_PREFIX + ".txt";
    }

    @Override
    public Resource getResourceByFullName(String name) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
        String format = formatter.format(now);
        String localFileName = "rt_" + format + ".txt";
        log.info("download and store file name = {}", localFileName);
        Resource resourceByHttp = getResourceByHttp(magUrl, localFileName);
        return resourceByHttp;

    }

    @Override
    public void clearCache() {
        log.debug("Clear cache fileNamesForUrl");
        cacheFileNamesForUrl.clear();
    }

    private Resource getResourceByHttp(String urlPath, String fileName) {

      int count = 0;
        while (true) {
            try {
                if (downloadFileByHttpToTemp(urlPath, fileName)) {
                    break;
                } else {
                    log.info("HTTP 200 and file is not ready. try again");
                }
            } catch (ResourceRetrievalException e) {
                log.error("error accessing magnetic", e);
                if (++count > retryCount) {
                    break;
                }
            }
        }

        return tempFileStorageManager.getResource(fileName);
    }

    private boolean downloadFileByHttpToTemp(String uri, String fileName) {
        try {
            URL url = new URL(uri);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            if (getLastTimestamp() != null) {
                String calculateBytes = calculateBytes(getLastTimestamp(), getCurrentTimeInUTC());
                log.info("last time: {} read next: {}", getLastTimestamp(), calculateBytes);
                urlConnection.setRequestProperty("Range", "bytes=-" + calculateBytes);
            }

            urlConnection.connect();
            log.info("Response Code: " + urlConnection.getResponseCode());
            log.info("Content-Length: " + urlConnection.getContentLengthLong());
            if (getLastTimestamp() != null && urlConnection.getResponseCode() == 200 && urlConnection.getContentLengthLong() == 0L) {
                return false; // delta is not available
            }
            InputStream inputStream = urlConnection.getInputStream();
            String targetFilePath = tempFileStorageManager.getDataTempDirectory() + fileName;
            saveInputStreamToTempFile(inputStream, targetFilePath);
            return true;
        } catch (Exception e) {
            throw new ResourceRetrievalException("Error getting file from: " + uri, e);
        }
    }

    private LocalDateTime getCurrentTimeInUTC() {
        return LocalDateTime.now().minusHours(3);
    }

    private String calculateBytes(LocalDateTime prevTimestamp, LocalDateTime currentTimestamp) {
        long numOfSeconds = Duration.between(prevTimestamp, currentTimestamp).getSeconds();
        return Long.valueOf(36 * (numOfSeconds)).toString();
    }


    private CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        return httpClientBuilder.build();
    }

    private void saveInputStreamToTempFile(InputStream inputStream, String targetFilePath) {
        try {
            FileUtils.copyInputStreamToFile(inputStream, new File(targetFilePath));
        } catch (IOException e) {
            throw new ResourceRetrievalException("Error save temp-file to: " + targetFilePath, e);
        }
    }

    private void checkStatusCode(int statusCode) {
        if (statusCode == 404) {
            throw new ResourceNotFoundException("statusCode = " + statusCode + " file not found");
        }

        if (statusCode != 200) {
            throw new ResourceRetrievalException("statusCode = " + statusCode + " error getting file");
        }
    }
}
