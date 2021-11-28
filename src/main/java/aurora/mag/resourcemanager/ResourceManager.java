package aurora.mag.resourcemanager;

import aurora.mag.config.ImportProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.core.io.Resource;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class ResourceManager {

    public static final String PRICES_FILENAME_PREFIX = "prices";
    public static final String DELTA_PRICES_FILENAME_PREFIX = "prices_delta";
    public static final String AVAILABILITY_FILENAME_PREFIX = "products_availability";
    public static final String DELTA_AVAILABILITY_FILENAME_PREFIX = "products_availability_delta";
    public static final String CATALOG_PARAMS_FILENAME_PREFIX = "catalog_params";
    //AURORA
    public static final String RT_FILENAME_PREFIX = "rt";


    public static final String ATTRS_FILENAME_PREFIX = "mc_catalog_properties";
    public static final String TIMEZONE_FILENAME_PREFIX = "timezone";
    public static final String COLLECTIONS_FILENAME_PREFIX = "collections";
    public static final String PROMO_FILENAME_PREFIX = "promo_info";
    public static final String SEPARATOR = "_";
    public static final String XML_EXTENSION = ".xml";

    public static final String DATE_PATTERN_DASH = "yyyy-MM-dd";
    public static final String DATE_TIME_PATTERN_DASH = "yyyy-MM-dd_HH-mm";
    public static final String DATE_PATTERN_UNDERSCORE = "yyyy_MM_dd";
    public final DateFormat dateFormatDash = new SimpleDateFormat(DATE_PATTERN_DASH);
    public final DateFormat dateFormatUnderscore = new SimpleDateFormat(DATE_PATTERN_UNDERSCORE);
    private LocalDateTime lastTimestamp = null;
    private Date dataImportDate;
    private String dataImportDatePrefx;
    private boolean simulateAsToday;

    public ResourceManager(ImportProperties properties) throws ParseException {
        dataImportDatePrefx = properties.importDataByDate
        //dataImportDate = parseDateIfExist(properties.importDataByDate, dateFormatDash);
        ;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        if (properties.getImportDataFromTimestamp() != null && !properties.getImportDataFromTimestamp().isEmpty()) {
            lastTimestamp = LocalDateTime.parse(properties.getImportDataFromTimestamp(), formatter);
        }
        simulateAsToday = properties.simulateAsToday;
        log.info("timestamp = {}", lastTimestamp);
        log.debug("dataImportDate = {}", dataImportDate);
    }

    public void clearCache() {
        //do nothing because there is no default cache
    }

    public abstract List<String> getResourceNamesByPrefix(String prefix);

    public abstract Resource getResourceByFullName(String name);

    private Resource getResourceByPrefix(String prefix) {
        List<String> resourceNames = getResourceNamesByPrefix(prefix);
        if (resourceNames.size() > 1) {
            log.warn("more than one resource found  by prefix = {}. resourceNames = {}", prefix, resourceNames);
        }
        return getFirstResourceByNameFromNamesList(resourceNames);
    }

    private Resource getFirstResourceByNameFromNamesList(List<String> resourceNames) {
        String firstResourceName = resourceNames.stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Can't find files at path = "));
        return getResourceByFullName(firstResourceName);
    }
    private List<Resource> getResourcesByNameFromNamesList(List<String> resourceNames) {
        return  resourceNames.stream()
                .map(this::getResourceByFullName).collect(Collectors.toList());
    }

    public String getMagneticFileLocation(Date executionJobDate) {
        return RT_FILENAME_PREFIX + SEPARATOR + dateFormatDash.format(executionJobDate);
    }


    public String getImportDate() {
        return getDataImportDateOrYesterday(dateFormatDash);
    }

    public boolean simulateAsToday() {
        return simulateAsToday;
    }

    public Resource getMagneticFile() {
        List<String> resourceNames = getResourceNamesByPrefixForTodayOrYesterday(RT_FILENAME_PREFIX + SEPARATOR, dateFormatDash);
        return getFirstResourceByNameFromNamesList(resourceNames);
    }
    public List<Resource> getMagneticFiles() {
        List<String> resourceNames = getResourceNamesByPrefixForTodayOrYesterday(RT_FILENAME_PREFIX + SEPARATOR, dateFormatDash);
        return getResourcesByNameFromNamesList(resourceNames);
    }

    private List<String> getResourceNamesByPrefixForTodayOrYesterday(String prefix, DateFormat dateFormat) {
        List<String> resourceNames = getResourceNamesByPrefix(prefix + getDataImportDateOrToday(dateFormat));
        if (resourceNames.isEmpty()) {
            resourceNames = getResourceNamesByPrefix(prefix + getDataImportDateOrYesterday(dateFormat));
        }
        return resourceNames;
    }

    private Date parseDateIfExist(String strDate, DateFormat dateFormat) throws ParseException {
        return !StringUtils.isBlank(strDate) ? dateFormat.parse(strDate) : null;
    }

    public Date getDataImportDate() {
        return dataImportDate;
    }


    public String getDataImportDateOrYesterday(DateFormat dateFormat) {
        return getDataImportDateOrDefault(dateFormat, yesterday());
    }

    public String getDataImportDateOrToday(DateFormat dateFormat) {
        //return getDataImportDateOrDefault(dateFormat, new Date());
        return getDataImportDateOrDefault(dateFormat.format(new Date()));
    }

    private String getDataImportDateOrDefault(DateFormat dateFormat, Date defaultValue) {
        Date dateForImport = (dataImportDate != null) ? dataImportDate : defaultValue;
        return dateFormat.format(dateForImport);
    }

    private String getDataImportDateOrDefault(String defaultValue) {
        return (dataImportDatePrefx != null) ? dataImportDatePrefx : defaultValue;

    }

    private Date yesterday() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return cal.getTime();
    }

    public LocalDateTime getDateTimeFromFileName(String fileName, Integer prefixLength, String dateTimePattern) {
        try {
            String strDateTime = fileName.substring(prefixLength, prefixLength + dateTimePattern.length());
            DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                    .appendPattern(dateTimePattern)
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.SMART);
            return LocalDateTime.parse(strDateTime, formatter);
        } catch (Exception e) {
            log.error(String.format("Error getDateTimeFromFileName(fileName = %s, prefixLength = %s)", fileName, prefixLength), e);
        }
        return LocalDateTime.MIN;
    }

    private List<String> sortResourceNamesByDateTime(List<String> resourceNames, String filePrefix, String dateTimePattern) {
        return resourceNames.stream()
                .sorted(Comparator.comparing(name -> getDateTimeFromFileName(name, filePrefix.length(), dateTimePattern)))
                .collect(Collectors.toList());
    }

    private LocalDateTime getResourceDateFromResource(Resource resource, String prefix) {
        return getDateTimeFromFileName(resource.getFilename(), prefix.length(), DATE_TIME_PATTERN_DASH);
    }

    public LocalDateTime getLastTimestamp() {
        return lastTimestamp;
    }

    public LocalDateTime getCurrentTimeInUTC() {
        return LocalDateTime.now().minusHours(3);
    }

    public void setLastTimestamp(LocalDateTime lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public boolean isLocal() {
        return this instanceof LocalResourceManager;
    }
}
