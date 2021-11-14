package top.keiskeiframework.file.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import top.keiskeiframework.file.annotations.MergingChunks;
import top.keiskeiframework.file.annotations.Upload;
import top.keiskeiframework.file.annotations.UploadBlobPart;
import top.keiskeiframework.file.annotations.UploadPart;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 *
 * </p>
 *
 * @author ：陈加敏 right_way@foxmail.com
 * @since ：2019/12/9 15:09
 */
@Data
@NoArgsConstructor
public class MultiFileInfo implements Serializable {

    private static final long serialVersionUID = 3563871956771137575L;
    /**
     * 文件名称
     */
    @NotBlank(groups = {UploadPart.class, UploadBlobPart.class, MergingChunks.class})
    private String fileName;

    /**
     * 文件大小
     */
    @NotNull(groups = {UploadPart.class, UploadBlobPart.class})
    private Long fileSize;

    /**
     * 总分片数
     */
    @NotNull(groups = {UploadPart.class, UploadBlobPart.class})
    @Min(value = 1, groups = {UploadPart.class, UploadBlobPart.class})
    private Integer partCount;

    /**
     * 当前分片下标
     */
    @NotNull(groups = {UploadPart.class, UploadBlobPart.class})
    @Min(value = 0, groups = {UploadPart.class, UploadBlobPart.class})
    private Integer index;

    /**
     * 当前文件分片
     */
    @NotNull(groups = {UploadPart.class, Upload.class})
    private MultipartFile file;

    /**
     * 当前文件分片byte 的 base64 String
     */
    @NotNull(groups = {UploadBlobPart.class})
    private String blobBase64;

    public MultiFileInfo(MultipartFile file) {
        this.file = file;
    }
}
