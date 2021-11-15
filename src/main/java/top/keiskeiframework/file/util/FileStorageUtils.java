package top.keiskeiframework.file.util;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import top.keiskeiframework.file.dto.MultiFileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * <p>
 *
 * </p>
 *
 * @author ：陈加敏 right_way@foxmail.com
 * @since ：2019/11/1 21:34
 */
public class FileStorageUtils {


    /**
     * 获取文件MD5名称, 降低文件重复
     *
     * @param file 文件信息
     * @return .
     */
    public static String getFileName(File file) throws IOException {
        String fileName = file.getName();
        String md5 = DigestUtils.md5DigestAsHex(new FileInputStream(file));
        if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(md5)) {
            throw new RuntimeException("fileName get error!");
        }
        return getFileName(fileName, md5);
    }

    /**
     * 获取文件名称
     *
     * @param fileName 原文件名称
     * @param md5      文件MD5
     * @return .
     */
    private static String getFileName(String fileName, String md5) {
        return fileName.replaceAll("[\\s\\S]+\\.(.*?)", md5 + ".$1").toLowerCase();
    }


//    public static boolean validFileType(MultiFileInfo fileInfo)  {
//
//        String fileSuffix = getFileSuffix(fileInfo);
//        try {
//            String fileMagicNumber = FileTypeEnum.get(fileSuffix);
//            if (!StringUtils.isEmpty(fileMagicNumber)) {
//                InputStream is = fileInfo.getFile().getInputStream();
//                if (is.available() > 0) {
//                    byte[] b = new byte[fileMagicNumber.length() / 2];
//                    is.read(b);
//                    if (getHex(b).equalsIgnoreCase(fileMagicNumber)) {
//                        return true;
//                    } else {
//                        System.out.println(fileSuffix + " : " + getHex(b));
//                    }
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    /**
     * 获取16进制表示的魔数
     *
     * @param data 字节数组形式的文件数据
     * @return .
     */
    public static String getHex(byte[] data) {
        //提取文件的魔数
        StringBuilder magicNumber = new StringBuilder();
        //一个字节对应魔数的两位
        for (byte b : data) {
            magicNumber.append(Integer.toHexString(b >> 4 & 0xF));
            magicNumber.append(Integer.toHexString(b & 0xF));
        }

        return magicNumber.toString().toUpperCase();
    }
}