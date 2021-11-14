package top.keiskeiframework.file.constants;

import top.keiskeiframework.file.dto.FileInfo;
import top.keiskeiframework.file.enums.FileUploadType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileConstants {

    public static Map<FileUploadType, List<FileInfo>> FILE_CACHE = new ConcurrentHashMap<>();
    public static final String TEMP_SUFFIX = ".temp";
}
