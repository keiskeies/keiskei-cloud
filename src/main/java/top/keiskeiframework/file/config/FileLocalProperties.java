package top.keiskeiframework.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.keiskeiframework.file.enums.FileUploadType;

/**
 * <p>
 * 文件管理配置类
 * </p>
 *
 * @author 陈加敏 right_way@foxmail.com
 * @since 2019/11/1 0:46
 */
@Data
@Component
@ConfigurationProperties(prefix = "keiskei.file")
public class FileLocalProperties {
    /**
     * 文件上传路径
     */
    private String path = "/tmp/file/";

    public String getConcatPath(FileUploadType path) {
        return this.path + path.path();
    }
    public String getConcatTempPath(FileUploadType path) {
        return this.path + path.temp();
    }
}
