package top.keiskeiframework.file.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import top.keiskeiframework.file.config.FileLocalProperties;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.service.FileStorageService;
import top.keiskeiframework.file.util.MultiFileUtils;

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class FileScheduled {

    @Autowired
    @Lazy
    private FileStorageService fileStorageService;
    @Autowired
    private FileLocalProperties fileLocalProperties;


    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void getFileList() {
        for (FileUploadType fileUploadType : FileUploadType.values()) {
            MultiFileUtils.checkDir(fileLocalProperties.getConcatPath(fileUploadType));
            fileStorageService.getFileInfo(fileUploadType);
        }
    }


}
