package top.keiskeiframework.file.service;

import top.keiskeiframework.file.enums.FileUploadType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

public interface FileShowService {

    Pattern PROCESS_PARAMS_PATTERN = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");

    void show(String path, String fileName, String process, HttpServletRequest request, HttpServletResponse response);

    void show(String path, String fileName, HttpServletRequest request, HttpServletResponse response);
}
