package top.keiskeiframework.file.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.keiskeiframework.common.exception.BizException;
import top.keiskeiframework.common.vo.R;
import top.keiskeiframework.file.config.FileLocalProperties;
import top.keiskeiframework.file.constants.FileConstants;
import top.keiskeiframework.file.dto.FileInfo;
import top.keiskeiframework.file.dto.MultiFileInfo;
import top.keiskeiframework.file.dto.Page;
import top.keiskeiframework.file.enums.FileStorageExceptionEnum;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.util.FileShowUtils;
import top.keiskeiframework.file.util.MultiFileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 本地文件服务类
 * </p>
 *
 * @author ：陈加敏 right_way@foxmail.com
 * @since ：2019/12/9 22:06
 */
@Service
@Slf4j
public class FileStorageService {

    @Autowired
    private FileLocalProperties fileLocalProperties;
    private static final int PAGE_SIZE = 20;


    public FileInfo upload(MultiFileInfo fileInfo, FileUploadType type) {
        File file = MultiFileUtils.upload(fileInfo, fileLocalProperties.getConcatPath(type));
        FileInfo result = getFileInfo(file, type);
        FileConstants.FILE_CACHE.get(type).add(result);
        return result;
    }


    public void uploadPart(MultiFileInfo fileInfo, FileUploadType type) {
        try {
            //切片上传
            MultiFileUtils.savePartFile(fileInfo, fileLocalProperties.getConcatPath(type));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(FileStorageExceptionEnum.FILE_UPLOAD_FAIL);
        }
    }

    public void uploadBlobPart(MultiFileInfo fileInfo, FileUploadType type) {
        try {
            //切片上传
            MultiFileUtils.saveBlobPartFile(fileInfo, fileLocalProperties.getConcatPath(type));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BizException(FileStorageExceptionEnum.FILE_UPLOAD_FAIL);
        }
    }


    public FileInfo mergingPart(MultiFileInfo fileInfo, FileUploadType type) {
        File file = MultiFileUtils.mergingParts(fileInfo, fileLocalProperties.getConcatPath(type));
        FileInfo result = getFileInfo(file, type);
        FileConstants.FILE_CACHE.get(type).add(0, result);
        return result;
    }


    public FileInfo exist(String fileName, FileUploadType type) {
        File file = MultiFileUtils.exitFile(fileLocalProperties.getConcatPath(type), fileName);
        assert file != null;
        return getFileInfo(file, type);
    }


    public void delete(String fileName, FileUploadType type, Integer index) {
        File file = new File(fileLocalProperties.getConcatPath(type), fileName);
        file.delete();
        if (null != index) {
            FileConstants.FILE_CACHE.get(type).remove(index.intValue());
        } else {
            Iterator<FileInfo> fileInfoIterable = FileConstants.FILE_CACHE.get(type).iterator();
            while (fileInfoIterable.hasNext()) {
                if (fileInfoIterable.next().getName().equals(fileName)) {
                    fileInfoIterable.remove();
                    break;
                }
            }

        }
    }

    public Page<FileInfo> list(FileUploadType type, int page) {
        int start = (page - 1) * PAGE_SIZE;
        int end = page * PAGE_SIZE;

        List<FileInfo> fileInfoCache = FileConstants.FILE_CACHE.get(type);
        int total = fileInfoCache.size();
        end = Math.min(end, total);
        List<FileInfo> data = fileInfoCache.subList(start, end);
        return new Page<>(total, data);
    }


    public void show(String fileName, FileUploadType type, String process, HttpServletRequest request, HttpServletResponse response) {

        try {
            if (null == exist(fileName, type)) {
                throw new RuntimeException(FileStorageExceptionEnum.FILE_DOWN_FAIL.getMsg());
            }
            FileShowUtils.show(fileLocalProperties.getConcatPath(type), fileName, type, process, request, response);
        } catch (IOException e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            try {
                response.getWriter().write(JSON.toJSONString(R.failed(FileStorageExceptionEnum.FILE_DOWN_FAIL)));
            } catch (IOException ignored) {
            }
        }
    }

    public void getFileInfo(FileUploadType type) {

        String path = fileLocalProperties.getConcatPath(type);

        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files && files.length > 0) {
                List<FileInfo> result = new ArrayList<>(files.length);
                for (File file1 : files) {
                    if (file1.isDirectory() || file1.getName().endsWith(FileConstants.TEMP_SUFFIX)) {
                        continue;
                    }
                    FileInfo fileInfo = getFileInfo(file1, type);
                    result.add(fileInfo);
                    log.info(JSON.toJSONString(fileInfo));
                }
                FileConstants.FILE_CACHE.put(type, result);
                return;
            }

        }
        FileConstants.FILE_CACHE.put(type, new ArrayList<>());
    }

    /**
     * 判断是否输出文件路径
     *
     * @param file 文件名称
     * @return .
     */
    private FileInfo getFileInfo(File file, FileUploadType type) {
        String contentType = null;
        try {
            contentType = Files.probeContentType(Paths.get(file.getPath()));
        } catch (IOException ignored) {
        }

        String tsStr = null;
        try {
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tsStr = sdf.format(file.lastModified());
        } catch (Exception ignored) {
        }
        return new FileInfo()
                .setName(file.getName())
                .setUrl("/api/file/" + type + "/show/" + file.getName())
                .setContentType(contentType)
                .setLength(file.length())
                .setUploadTime(tsStr);
    }

}
