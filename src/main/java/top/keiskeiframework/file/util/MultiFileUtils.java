package top.keiskeiframework.file.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import top.keiskeiframework.common.exception.BizException;
import top.keiskeiframework.file.dto.MultiFileInfo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
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


    public static synchronized void savePartFile(MultiFileInfo fileInfo, String tmpPath) {
        String fileDirName = getTmpName(fileInfo, tmpPath);
        //禁用FileInfo.exists()类, 防止缓存导致并发问题
        File tempFile = new File(fileDirName);
        if (!(tempFile.exists() && tempFile.isFile())) {
            //上锁
            REENTRANT_LOCK.lock();
            try {
                if (!(tempFile.exists() && tempFile.isFile())) {
                    MultiFileUtils.readySpaceFile(fileInfo, tempFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                REENTRANT_LOCK.unlock();
            }
            //释放锁

        }
        tempFile = new File(fileDirName);
        MultiFileUtils.spaceFileWriter(tempFile, fileInfo);
    }



    /**
     * 合并分片文件
     *
     * @param fileInfo 文件信息
     * @param tmpPath  临时路径
     * @param path     真实路径
     * @return .
     */
    public static File mergingParts(MultiFileInfo fileInfo, String tmpPath, String path) {
        checkDir(path);
        File tempFile = null;
        try {
            String fileDirName = getTmpName(fileInfo, tmpPath);
            tempFile = new File(fileDirName);
            if (tempFile.exists() && tempFile.isFile()) {
                String targetDirName = fileInfo.getFileName();
                File targetFile = new File(path, targetDirName);
                if (tempFile.renameTo(targetFile)) {
                    return targetFile;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != tempFile) {
                if (tempFile.delete()) {
                    log.info("temp file {} delete success", tempFile.getName());
                }
            }
        }
        return null;
    }


    public static synchronized void saveBlobPartFile(MultiFileInfo fileInfo, String tmpPath) {
        String fileTempPath = getBlobTmpPath(fileInfo, tmpPath);
        File fileTempPathDir = new File(fileTempPath);
        if (!fileTempPathDir.exists()) {
            if(!fileTempPathDir.mkdirs()) {
                throw new BizException("mkdir " + fileTempPathDir + " fail");
            }
        }
        String fileTempNamePrefix = getBlobTmpNamePrefix(fileInfo, tmpPath);

        // 临时文件地址
        File tempFile = new File(fileTempNamePrefix + fileInfo.getIndex());
        // 临时文件数据
        byte[] blobs = Base64.decodeBase64(fileInfo.getBlobBase64());
        try (
                FileOutputStream out = new FileOutputStream(tempFile);
        ) {
            out.write(blobs, 0, blobs.length);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                tempFile.delete();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 合并分片文件
     *
     * @param fileInfo 文件信息
     * @param tmpPath  临时路径
     * @param path     真实路径
     * @return .
     */
    public static File mergingBlobParts(MultiFileInfo fileInfo, String tmpPath, String path) {
        checkDir(path);
        // xxx.temp
        String fileTempPath = getBlobTmpPath(fileInfo, tmpPath);
        String fileTempNamePrefix = getBlobTmpNamePrefix(fileInfo, tmpPath);

        String targetDirName = fileInfo.getFileName();
        File targetFile = new File(path, targetDirName);


        long startPointer = 0;
        try {

            RandomAccessFile targetSpaceFile = new RandomAccessFile(targetFile, "rws");
            targetSpaceFile.setLength(fileInfo.getFileSize());
            targetSpaceFile.close();

            for (int i = 0; i < fileInfo.getPartCount(); i++) {
                // xxx.temp1
                File fileTemp = new File(fileTempNamePrefix + i);

                try (FileInputStream in = new FileInputStream(fileTemp);
                     FileChannel inChannel = in.getChannel();
                     FileOutputStream out = new FileOutputStream(targetFile, true);
                     FileChannel outChannel = out.getChannel()) {

                    outChannel.transferFrom(inChannel, startPointer, fileTemp.length());
                    startPointer += fileTemp.length();
                } catch (IOException e) {
                    throw new BizException(e.getMessage());
                }

            }
        } catch (IOException e) {
            throw new BizException(e.getMessage());
        } finally {
            File fileTemp = new File(fileTempPath);
            try {
                fileTemp.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetFile;
    }


    /**
     * 获取文件临时名称
     *
     * @param fileInfo 文件分片信息
     * @param tmpPath  临时目录
     * @return .
     */
    private static String getTmpName(MultiFileInfo fileInfo, String tmpPath) {

        String id = fileInfo.getId();
        return tmpPath + id + "_" + fileInfo.getFileName() + ".temp";
    }

    /**
     * 获取文件临时名称
     *
     * @param fileInfo 文件分片信息
     * @param tmpPath  临时目录
     * @return .
     */
    private static String getBlobTmpPath(MultiFileInfo fileInfo, String tmpPath) {

        return tmpPath + fileInfo.getId();
    }

    /**
     * 获取文件临时名称前缀
     *
     * @param fileInfo 文件分片信息
     * @param tmpPath  临时目录
     * @return .
     */
    private static String getBlobTmpNamePrefix(MultiFileInfo fileInfo, String tmpPath) {

        return tmpPath + fileInfo.getId() + "/" + fileInfo.getFileName() + ".temp";
    }

    /**
     * 创建空目标文件
     *
     * @throws IOException .
     */
    private static void readySpaceFile(MultiFileInfo fileInfo, File tempFile) throws IOException {
        RandomAccessFile targetSpaceFile = new RandomAccessFile(tempFile, "rws");
        targetSpaceFile.setLength(fileInfo.getFileSize());
        targetSpaceFile.close();
    }

    /**
     * 向空文件写入二进制数据
     */
    private static void spaceFileWriter(File tempFile, MultiFileInfo fileInfo) {
        long startPointer = getBlobFileWriterStartPointer(fileInfo);

        byte[] blobs = Base64.decodeBase64(fileInfo.getBlobBase64());
        try (
                FileOutputStream out = new FileOutputStream(tempFile);
        ) {
            out.write(blobs, 0, blobs.length);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                tempFile.delete();
            } catch (Exception ignored) {
            }
        }

        try (
                FileChannel inChannel = ((FileInputStream) fileInfo.getFile().getInputStream()).getChannel();
                FileOutputStream out = new FileOutputStream(tempFile, true);
                FileChannel outChannel = out.getChannel()
        ) {
            outChannel.transferFrom(inChannel, startPointer, blobs.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 计算指针开始位置
     *
     * @param fileInfo:分片实体类
     */
    private synchronized static Long getFileWriterStartPointer(MultipartFile file, MultiFileInfo fileInfo) {
        // TODO Auto-generated method stub
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
    private synchronized static Long getBlobFileWriterStartPointer(MultiFileInfo fileInfo) {
        // TODO Auto-generated method stub
        long chunkSize = 5 * 1024 * 1024;
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
