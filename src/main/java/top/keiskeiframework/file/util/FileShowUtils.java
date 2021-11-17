package top.keiskeiframework.file.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import top.keiskeiframework.file.constants.FileConstants;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.process.FileProcess;
import top.keiskeiframework.file.process.ImageProcess;
import top.keiskeiframework.file.process.VideoProcess;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author James Chen right_way@foxmail.com
 * @since 2020/6/6 16:10
 */
@Slf4j
public class FileShowUtils {


    public static void show(String path, String fileName, FileUploadType type, String process, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contentType = Files.probeContentType(Paths.get(path + fileName));
        response.setContentType(contentType);

        if (StringUtils.hasText(process)) {
            String[] processes = process.split("/");
            if (processes.length > 1) {
                FileProcess fileProcess = new FileProcess();

                switch (type) {
                    case image:
                        fileProcess.setImageProcess(new ImageProcess(processes));
                        image2Image(path, fileName, fileProcess.getImageProcess(), request, response);
                        return;
                    case video:
                        fileProcess.setVideoProcess(new VideoProcess(processes));
                        video2Image(path, fileName, fileProcess.getVideoProcess(), request, response);
                        return;
                }
            }
        }
        switch (type) {
            case image:
                showImage(path, fileName, request, response);
            case video:
                showVideo(path, fileName, request, response);
        }

    }


    public static void image2Image(String path, String fileName, ImageProcess imageProcess, HttpServletRequest request, HttpServletResponse response) throws IOException {

        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(request.getQueryString());
        String params = matcher.replaceAll("");
        File file = new File(path, fileName + params + FileConstants.TEMP_SUFFIX);
        if (file.exists() && file.length() > 0) {
            showImage(path, fileName + params + FileConstants.TEMP_SUFFIX, request, response);
        } else {
            try {
                String contentType = null;
                Thumbnails.Builder<File> thumbnails = Thumbnails.of(path + fileName);
                // 获取文件原始宽高
                BufferedImage bi = Thumbnails.of(path + fileName).scale(1D).asBufferedImage();

                int srcImgWidth = bi.getWidth();
                int srcImgHeight = bi.getHeight();

                // 图片处理
                if (null != imageProcess.getResize()) {
                    imageProcess.getResize().resize(thumbnails, srcImgWidth, srcImgHeight);
                } else {
                    thumbnails.scale(1D);
                }
                if (null != imageProcess.getQuality()) {
                    // 图片质量压缩
                    imageProcess.getQuality().quality(thumbnails, srcImgWidth, srcImgHeight);
                }
                if (null != imageProcess.getCircle()) {
                    // 图片裁剪压缩
                    imageProcess.getCircle().circle(thumbnails, srcImgWidth, srcImgHeight);
                }
                if (null != imageProcess.getIndexcrop()) {
                    // 图片分块裁剪
                    imageProcess.getIndexcrop().indexcrop(thumbnails, srcImgWidth, srcImgHeight);
                }
                if (null != imageProcess.getWatermark()) {
                    // 图片水印
                    imageProcess.getWatermark().watermark(thumbnails, srcImgWidth, srcImgHeight);
                }
                if (null != imageProcess.getFormat()) {
                    // 图片格式转换
                    thumbnails.outputFormat(imageProcess.getFormat());
                    contentType = "image/" + imageProcess.getFormat();

                }
                if (null != imageProcess.getRotate()) {
                    // 图片旋转
                    thumbnails.rotate(imageProcess.getRotate());
                }

                if (!StringUtils.hasText(contentType)) {
                    contentType = "image/jpeg";
                }
                response.setContentType(contentType);
                OutputStream os = response.getOutputStream();
                thumbnails.toOutputStream(os);

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    thumbnails.toOutputStream(fos);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } catch (Exception e) {
                showImage(path, fileName, request, response);

            }
        }
    }

    public static void video2Image(String path, String fileName, VideoProcess videoProcess, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
        Matcher matcher = pattern.matcher(request.getQueryString());
        String params = matcher.replaceAll("");
        File file = new File(path, fileName + params + FileConstants.TEMP_SUFFIX);
        if (file.exists() && file.length() > 0) {
            showImage(path, fileName + FileConstants.TEMP_SUFFIX, request, response);
        } else {
            if (null != videoProcess.getSnapshot()) {
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    BufferedImage bufferedImage = videoProcess.getSnapshot().snapshot(new File(path + fileName), response, fos);
                    String f = StringUtils.hasText(videoProcess.getSnapshot().getF()) ? videoProcess.getSnapshot().getF() : "jpg";
                    response.setContentType("image/" + f);
                    OutputStream os = response.getOutputStream();
                    ImageIO.write(bufferedImage, f, os);
                    ImageIO.write(bufferedImage, f, fos);
                    return;
                }
            }
            showVideo(path, fileName, request, response);
        }

    }

    public static void showImage(String path, String fileName, HttpServletRequest request, HttpServletResponse response) {
        File file = new File(path, fileName);
        try {
            String contentType = request.getServletContext().getMimeType(fileName);
            if (!StringUtils.hasText(contentType)) {
                contentType = Files.probeContentType(Paths.get(path + fileName));
            }
            if (!StringUtils.hasText(contentType)) {
                contentType = "image/jpeg";
            }
            response.setContentType(contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (
                InputStream is = new FileInputStream(file);
                ServletOutputStream os = response.getOutputStream()
        ) {
            int bufferLength;
            byte[] buffer = new byte[StreamUtils.BUFFER_SIZE];
            while ((bufferLength = is.read(buffer)) != -1) {
                os.write(buffer, 0, bufferLength);
            }
        } catch (IOException e) {
            e.printStackTrace();
            response.reset();
            response.setContentType("application/json;charset=utf-8");
        }
    }


    public static void showVideo(String path, String fileName, HttpServletRequest request, HttpServletResponse response) {
        fileName = path + fileName;
        File file = new File(fileName);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(fileName);
            if (!StringUtils.hasText(contentType)) {
                contentType = Files.probeContentType(Paths.get(path + fileName));
            }
            response.setContentType(contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String range = request.getHeader("Range");
        log.info("current request rang:" + range);
        //开始下载位置
        long startByte = 0;
        //结束下载位置
        long endByte = file.length() - 1;
        log.info("文件开始位置：{}，文件结束位置：{}，文件总长度：{}", startByte, endByte, file.length());

        //有range的话
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //判断range的类型
                if (ranges.length == 1) {
                    //类型一：bytes=-2343
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //类型二：bytes=2343-
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //类型三：bytes=22-2343
                else if (ranges.length == 2) {
                    startByte = Long.parseLong(ranges[0]);
                    endByte = Long.parseLong(ranges[1]);
                }

            } catch (NumberFormatException e) {
                startByte = 0;
                endByte = file.length() - 1;
                log.error("Range Occur Error,Message:{}", e.getLocalizedMessage());
            }
        }


        //要下载的长度
        long contentLength = endByte - startByte + 1;

        // 解决下载文件时文件名乱码问题
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        fileName = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

        response.setHeader("Accept-Ranges", "bytes");
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentType(contentType);
        response.setHeader("Content-Type", contentType);
        response.setHeader("Content-Disposition", "inline;filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());

        BufferedOutputStream outputStream = null;
        RandomAccessFile randomAccessFile = null;
        //已传送数据大小
        long transmitted = 0;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            outputStream = new BufferedOutputStream(response.getOutputStream());
            byte[] buff = new byte[4096];
            int len = 0;
            randomAccessFile.seek(startByte);
            while ((transmitted + len) <= contentLength && (len = randomAccessFile.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
                transmitted += len;
            }
            if (transmitted < contentLength) {
                len = randomAccessFile.read(buff, 0, (int) (contentLength - transmitted));
                outputStream.write(buff, 0, len);
                transmitted += len;
            }

            outputStream.flush();
            response.flushBuffer();
            randomAccessFile.close();
            log.info("下载完毕：" + startByte + "-" + endByte + "：" + transmitted);
        } catch (ClientAbortException e) {
            log.warn("用户停止下载：" + startByte + "-" + endByte + "：" + transmitted);
        } catch (IOException e) {
            log.error("用户下载IO异常，Message：{}", e.getLocalizedMessage());
        } finally {
            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
