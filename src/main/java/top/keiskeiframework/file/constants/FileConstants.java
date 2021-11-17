package top.keiskeiframework.file.constants;

import top.keiskeiframework.file.dto.FileInfo;
import top.keiskeiframework.file.enums.FileUploadType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class FileConstants {

    public static final Map<FileUploadType, List<FileInfo>> FILE_CACHE = new ConcurrentHashMap<>();
    public static final String TEMP_SUFFIX = ".temp";
    public static final Pattern PROCESS_PARAMS_PATTERN = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
}
