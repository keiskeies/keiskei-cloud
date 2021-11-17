package top.keiskeiframework.file.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.keiskeiframework.common.exception.BizException;
import top.keiskeiframework.file.dto.MultiFileInfo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;


/**
 * <p>
 * 文件上传工具类
 * </p>
 *
 * @author ：陈加敏 right_way@foxmail.com
 * @since ：2019/9/24 17:16
 */
@Slf4j
public class MultiFileUtils {

    private static final ReentrantLock REENTRANT_LOCK = new ReentrantLock();

    public static File upload(MultiFileInfo fileInfo, String path) {
        checkDir(path);
        String fileName = fileInfo.getFileName();
        if (StringUtils.isEmpty(fileName)) {
            fileName = fileInfo.getFile().getOriginalFilename();
        }
        File file = new File(path + fileName);
        try (FileOutputStream os = new FileOutputStream(file)) {
            byte[] fileBytes = fileInfo.getFile().getBytes();
            os.write(fileBytes);
            os.flush();
            return file;
        } catch (Exception e1) {
            e1.printStackTrace();
            throw new RuntimeException("file upload fail!");
        }

    }


    /**
     * 检验文件夹是否存在
     *
     * @param path 文件夹路径
     */
    public static void checkDir(String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            if (!file.mkdirs()) {
                throw new RuntimeException("dir make fail!");
            }
            log.info("dir {} make success!", path);
        }
    }

    public static File exitFile(String path, String fileName) {
        File pathFile = new File(path);
        String[] fileList = pathFile.list();
        if (null != fileList && fileList.length > 0) {
            for (String file : fileList) {
                File fileTemp = new File(path, file);
                if (fileTemp.isFile()) {
                    if (file.contains(fileName)) {
                        return fileTemp;
                    }
                } else {
                    if (null != (fileTemp = exitFile(fileTemp.getPath(), fileName))) {
                        return fileTemp;
                    }
                }
            }
        }
        return null;
    }


    public static synchronized void savePartFile(MultiFileInfo fileInfo, String path) {
        File targetFile = initPartsFile(fileInfo, path);
        long startPointer = getFileWriterStartPointer(fileInfo.getFile(), fileInfo);

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


    public static synchronized void saveBlobPartFile(MultiFileInfo fileInfo, String path) {

        File targetFile = initPartsFile(fileInfo, path);

        try (
                FileOutputStream out = new FileOutputStream(targetFile, true);
                FileChannel outChannel = out.getChannel()
        ) {
            byte[] blobs = Base64.decodeBase64(fileInfo.getBlobBase64());
            ByteBuffer byteBuffer = ByteBuffer.wrap(blobs);
            long startPointer = getBlobFileWriterStartPointer(blobs.length, fileInfo);
            outChannel.write(byteBuffer, startPointer);
        } catch (IOException e) {
            e.printStackTrace();
            targetFile.deleteOnExit();
            throw new RuntimeException("file upload fail!");
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



    /**
     * 合并分片文件
     *
     * @param fileInfo 文件信息
     * @param path     真实路径
     * @return .
     */
    public static File mergingParts(MultiFileInfo fileInfo, String path) {
        return new File(path, fileInfo.getFileName());
    }

    /**
     * 计算指针开始位置
     *
     * @param fileInfo:分片实体类
     */
    private synchronized static Long getFileWriterStartPointer(MultipartFile file, MultiFileInfo fileInfo) {
        long chunkSize = file.getSize();
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

    /**
     * 计算指针开始位置
     *
     * @param fileInfo:分片实体类
     */
    private synchronized static Long getBlobFileWriterStartPointer(long chunkSize, MultiFileInfo fileInfo) {
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

}
