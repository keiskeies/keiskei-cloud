package top.keiskeiframework.file.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import top.keiskeiframework.file.constants.FileConstants;
import top.keiskeiframework.file.process.VideoProcess;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

@Slf4j
public class VideoFileShowUtils {
    public static void show(String path, String fileName, String process, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.hasText(process)) {
            String[] processes = process.split("/");
            if (processes.length > 1) {
                Matcher matcher = FileConstants.PROCESS_PARAMS_PATTERN.matcher(request.getQueryString());
                String params = matcher.replaceAll("");
                File tempFile = new File(path, fileName + params + FileConstants.TEMP_SUFFIX);
                if (tempFile.exists() && tempFile.length() > 0) {
                    ImageFileShowUtils.show(path, fileName + params + FileConstants.TEMP_SUFFIX, request, response);
                    return;
                } else {
                    VideoProcess videoProcess = new VideoProcess(processes);
                    if (null != videoProcess.getSnapshot()) {
                        try {
                            BufferedImage bufferedImage = videoProcess.getSnapshot().snapshot(new File(path + fileName));
                            String f = StringUtils.hasText(videoProcess.getSnapshot().getF()) ? videoProcess.getSnapshot().getF() : "jpg";
                            response.setContentType("image/" + f);
                            OutputStream os = response.getOutputStream();
                            ImageIO.write(bufferedImage, f, os);
                            try (FileOutputStream fos = new FileOutputStream(tempFile)){
                                ImageIO.write(bufferedImage, f, fos);
                            }
                            return;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        }
        show(path, fileName, request, response);
    }

    public static void show(String path, String fileName, HttpServletRequest request, HttpServletResponse response) {
        fileName = path + fileName;
        File file = new File(fileName);

        String range = request.getHeader("Range");
        log.info("current request rang:" + range);
        //??????????????????
        long startByte = 0;
        //??????????????????
        long endByte = file.length() - 1;
        log.info("?????????????????????{}????????????????????????{}?????????????????????{}", startByte, endByte, file.length());

        //???range??????
        if (range != null && range.contains("bytes=") && range.contains("-")) {
            range = range.substring(range.lastIndexOf("=") + 1).trim();
            String[] ranges = range.split("-");
            try {
                //??????range?????????
                if (ranges.length == 1) {
                    //????????????bytes=-2343
                    if (range.startsWith("-")) {
                        endByte = Long.parseLong(ranges[0]);
                    }
                    //????????????bytes=2343-
                    else if (range.endsWith("-")) {
                        startByte = Long.parseLong(ranges[0]);
                    }
                }
                //????????????bytes=22-2343
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

        //??????????????????
        long contentLength = endByte - startByte + 1;

        // ??????????????????????????????????????????
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        fileName = new String(fileNameBytes, 0, fileNameBytes.length, StandardCharsets.ISO_8859_1);

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(fileName);
            if (!StringUtils.hasText(contentType)) {
                contentType = Files.probeContentType(Paths.get(path + fileName));
            }
            response.setContentType(contentType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "inline;filename=" + fileName);
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + startByte + "-" + endByte + "/" + file.length());


        //?????????????????????
        long transmitted = 0;
        try (
                BufferedOutputStream outputStream = new BufferedOutputStream(response.getOutputStream());
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        ) {


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
            log.info("???????????????" + startByte + "-" + endByte + "???" + transmitted);
        } catch (IOException e) {
            log.warn("?????????????????????" + startByte + "-" + endByte + "???" + transmitted);
            log.error("????????????IO?????????Message???{}", e.getLocalizedMessage());
        }
    }

}
