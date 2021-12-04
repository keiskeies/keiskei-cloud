package top.keiskeiframework.file.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import top.keiskeiframework.common.vo.R;
import top.keiskeiframework.file.annotations.MergingChunks;
import top.keiskeiframework.file.annotations.UploadBlobPart;
import top.keiskeiframework.file.annotations.UploadPart;
import top.keiskeiframework.file.constants.FileConstants;
import top.keiskeiframework.file.dto.FileInfo;
import top.keiskeiframework.file.dto.MultiFileInfo;
import top.keiskeiframework.file.dto.Page;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.service.FileStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * <p>
 * 文件前端控制器
 * </p>
 *
 * @author James Chen right_way@foxmail.com
 * @since 2021/2/19 15:25
 */
@Controller
@CrossOrigin
@RequestMapping("/api/file/{type:image|video}")
public class FileStorageController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * ping
     *
     * @return .
     */
    @GetMapping("/ping")
    @ResponseBody
    public R ping() {
        return R.ok();
    }

    /**
     * 普通上传
     *
     * @param file 文件信息
     * @return .
     */
    @PostMapping("/upload")
    @ResponseBody
    public R<FileInfo> upload(
            MultipartFile file,
            @PathVariable FileUploadType type
    ) {
        return R.ok(fileStorageService.upload(file, type));
    }


    /**
     * 上传文件分片
     *
     * @param fileInfo 文件分片信息
     * @return .
     */
    @ResponseBody
    @PostMapping("/uploadPart")
    public R<Boolean> uploadPart(
            @Validated({UploadPart.class}) MultiFileInfo fileInfo,
            @PathVariable FileUploadType type
    ) {
        fileStorageService.uploadPart(fileInfo, type);
        return R.ok(true);
    }

    /**
     * 上传文件分片
     *
     * @param fileInfo 文件分片信息
     * @return .
     */
    @ResponseBody
    @PostMapping("/uploadBlobPart")
    public R<Boolean> uploadBlobPart(
            @RequestBody @Validated({UploadBlobPart.class}) MultiFileInfo fileInfo,
            @PathVariable FileUploadType type
    ) {
        fileStorageService.uploadBlobPart(fileInfo, type);
        return R.ok(true);
    }

    /**
     * 合并文件分片
     *
     * @param fileInfo 文件信息
     * @return .
     */
    @ResponseBody
    @PostMapping("/mergingPart")
    public R<FileInfo> mergingPart(
            @RequestBody @Validated({MergingChunks.class}) MultiFileInfo fileInfo,
            @PathVariable FileUploadType type
    ) {
        return R.ok(fileStorageService.mergingPart(fileInfo, type));
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return .
     */
    @DeleteMapping("/delete/{fileName}")
    @ResponseBody
    public R<FileInfo> delete(
            @PathVariable("fileName") String fileName,
            @RequestParam(required = false) Integer index,
            @PathVariable FileUploadType type
    ) {
        fileStorageService.delete(fileName.trim(), type, index);

        return R.ok();
    }

    @GetMapping("/list")
    @ResponseBody
    public R<Page<FileInfo>> list(
            @PathVariable FileUploadType type,
            @RequestParam(required = false, defaultValue = "0") @Min(0) Integer offset
    ) {
        return R.ok(fileStorageService.list(type, offset));
    }

    @GetMapping("/sort")
    @ResponseBody
    public R sort(@PathVariable FileUploadType type) {
        fileStorageService.sort(type);
        return R.ok();
    }


    /**
     * 获取图片
     *
     * @param fileName 文件类型
     * @param request  request
     * @param response response
     * @param process  文件处理参数
     */
    @GetMapping("/show/{fileName}")
    public void show(@PathVariable("fileName") String fileName,
                     HttpServletRequest request,
                     HttpServletResponse response,
                     @RequestParam(value = "x-oss-process", required = false) String process,
                     @PathVariable FileUploadType type
    ) {
        try {
            fileStorageService.show(fileName.trim(), type, process, request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
