package top.keiskeiframework.file.service;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.ch.ChannelInputStream;
import top.keiskeiframework.file.config.FileLocalProperties;
import top.keiskeiframework.file.constants.FileConstants;
import top.keiskeiframework.file.dto.FileInfo;
import top.keiskeiframework.file.dto.MultiFileInfo;
import top.keiskeiframework.file.dto.Page;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.util.FileStorageUtils;
import top.keiskeiframework.file.util.ImageFileShowUtils;
import top.keiskeiframework.file.util.VideoFileShowUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();


    public FileInfo upload(MultipartFile file, FileUploadType type) {

        File targetFile = new File(fileLocalProperties.getConcatPath(type), Objects.requireNonNull(file.getOriginalFilename()));

        try (
                InputStream is = file.getInputStream();
                FileOutputStream out = new FileOutputStream(targetFile, true);
        ) {

            byte[] buffer = new byte[2048];
            int len = 0;
            while ((len = is.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            targetFile.deleteOnExit();
            throw new RuntimeException("file upload fail!");
        }

        return getMd5FileInfo(targetFile, type);
    }


    public void uploadPart(MultiFileInfo fileInfo, FileUploadType type) {
        File targetFile = initPartsFile(fileInfo, fileLocalProperties.getConcatPath(type));
        long startPointer = getFileWriterStartPointer(fileInfo.getFile().getSize(), fileInfo);

        try (
                FileChannel inChannel = ((FileInputStream) fileInfo.getFile().getInputStream()).getChannel();
                FileOutputStream out = new FileOutputStream(targetFile, true);
                FileChannel outChannel = out.getChannel()
        ) {
            outChannel.transferFrom(inChannel, startPointer, fileInfo.getFile().getSize());
        } catch (IOException e) {
            e.printStackTrace();
            targetFile.deleteOnExit();
            throw new RuntimeException("file upload fail!");
        }
    }

    public void uploadBlobPart(MultiFileInfo fileInfo, FileUploadType type) {
        File targetFile = initPartsFile(fileInfo, fileLocalProperties.getConcatPath(type));

        try (
                FileOutputStream out = new FileOutputStream(targetFile, true);
                FileChannel outChannel = out.getChannel()
        ) {
            byte[] blobs = Base64.decodeBase64(fileInfo.getBlobBase64());
            ByteBuffer byteBuffer = ByteBuffer.wrap(blobs);
            long startPointer = getFileWriterStartPointer(blobs.length, fileInfo);
            outChannel.write(byteBuffer, startPointer);
        } catch (IOException e) {
            e.printStackTrace();
            targetFile.deleteOnExit();
            throw new RuntimeException("file upload fail!");
        }
    }


    public FileInfo mergingPart(MultiFileInfo fileInfo, FileUploadType type) {
        String path = fileLocalProperties.getConcatPath(type);
        File file = new File(path, fileInfo.getFileName());
        return getMd5FileInfo(file, type);
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

    public Page<FileInfo> list(FileUploadType type, int offset) {
        int end = offset + PAGE_SIZE;

        List<FileInfo> fileInfoCache = FileConstants.FILE_CACHE.get(type);
        int total = fileInfoCache.size();
        end = Math.min(end, total);
        List<FileInfo> data = fileInfoCache.subList(offset, end);
        return new Page<>(total, data);
    }


    public void sort(FileUploadType type) {
        String path = fileLocalProperties.getConcatPath(type);
        File fileDir = new File(path);
        File[] files = fileDir.listFiles();
        if (null != files && files.length > 0) {
            List<FileInfo> result = new ArrayList<>(files.length);
            for (File file : files) {
                if (file.isDirectory() || file.getName().endsWith(FileConstants.TEMP_SUFFIX)) {
                    continue;
                }
                result.add(getMd5FileInfo(file, type));
            }
            result = result.stream().sorted(Comparator.comparing(FileInfo::getUploadTime).reversed()).collect(Collectors.toList());
            FileConstants.FILE_CACHE.put(type, result);
        }
    }


    public void show(String fileName, FileUploadType type, String process, HttpServletRequest request, HttpServletResponse response) {

        String path = fileLocalProperties.getConcatPath(type);
        switch (type) {
            case image:
                ImageFileShowUtils.show(path, fileName, process, request, response);
            case video:
                VideoFileShowUtils.show(path, fileName, process, request, response);
        }
    }

    public void getFileInfoList(FileUploadType type) {

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
                result = result.stream()
                        .filter(distinctByKey(FileInfo::getName))
                        .sorted(Comparator.comparing(FileInfo::getUploadTime).reversed())
                        .collect(Collectors.toList());
                FileConstants.FILE_CACHE.put(type, result);
                return;
            }

        }
        FileConstants.FILE_CACHE.put(type, new ArrayList<>());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
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

    /**
     * 计算指针开始位置
     *
     * @param fileInfo:分片实体类
     */
    private static Long getFileWriterStartPointer(long chunkSize, MultiFileInfo fileInfo) {
        int currChunk = fileInfo.getIndex();
        int allChunks = fileInfo.getPartCount();
        long allSize = fileInfo.getFileSize();
        if (currChunk < (allChunks - 1)) {
            return chunkSize * currChunk;
        } else if (currChunk == (allChunks - 1)) {
            return allSize - chunkSize;
        } else {
            throw new RuntimeException("file part error!");
        }
    }

    private static File initPartsFile(MultiFileInfo fileInfo, String path) {
        File targetFile = new File(path, fileInfo.getFileName());
        //上锁
        if (!(targetFile.exists() && targetFile.isFile())) {
            //上锁
            REENTRANT_LOCK.lock();

            if (!(targetFile.exists() && targetFile.isFile())) {
                try (RandomAccessFile targetSpaceFile = new RandomAccessFile(targetFile, "rws")) {
                    targetSpaceFile.setLength(fileInfo.getFileSize());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            //释放锁
            REENTRANT_LOCK.unlock();
        }
        return targetFile;
    }


    private FileInfo getMd5FileInfo(File file, FileUploadType type) {
        String path = fileLocalProperties.getConcatPath(type);
        FileInfo result;
        if (type.getMd5Name()) {
            try {
                String fileName = FileStorageUtils.getMd5FileName(file);
                if (file.getName().equals(fileName)) {
                    throw new IOException("file name same");
                }
                File fileNew = new File(path + fileName);
                file.renameTo(fileNew);
                result = getFileInfo(fileNew, type);
                FileConstants.FILE_CACHE.get(type).add(0, result);
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        result = getFileInfo(file, type);
        FileConstants.FILE_CACHE.get(type).add(0, result);
        return result;
    }

}
