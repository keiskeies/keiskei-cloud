package top.keiskeiframework.file.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import top.keiskeiframework.file.config.FileLocalProperties;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.service.FileStorageService;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
@EnableScheduling
public class FileScheduled {

    @Autowired
    @Lazy
    private FileStorageService fileStorageService;
    @Autowired
    private FileLocalProperties fileLocalProperties;



    @Scheduled(cron = "0 */5 * * * ?")
    public void getFileList() {
        for (FileUploadType fileUploadType : FileUploadType.values()) {
            fileStorageService.getFileInfoList(fileUploadType);
        }
    }

    @PostConstruct
    public void checkDir() {
        for (FileUploadType fileUploadType : FileUploadType.values()) {
            File file = new File(fileLocalProperties.getConcatPath(fileUploadType));
            if (!file.exists() || !file.isDirectory()) {
                if (!file.mkdirs()) {
                    throw new RuntimeException("dir make fail!");
                }
            }
            fileStorageService.getFileInfoList(fileUploadType);
        }

    }


}
