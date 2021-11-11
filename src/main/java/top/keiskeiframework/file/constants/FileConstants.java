package top.keiskeiframework.file.constants;

import top.keiskeiframework.file.dto.FileInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileConstants {

    public static Map<String, List<FileInfo>> FILE_CACHE = new ConcurrentHashMap<>();
}
