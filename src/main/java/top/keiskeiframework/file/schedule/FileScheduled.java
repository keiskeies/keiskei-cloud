package top.keiskeiframework.file.schedule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import top.keiskeiframework.file.enums.FileUploadType;
import top.keiskeiframework.file.service.FileStorageService;

import javax.annotation.PostConstruct;

@Configuration
@EnableScheduling
public class FileScheduled {

    @Autowired
    private FileStorageService fileStorageService;


    @PostConstruct
    @Scheduled(cron = "0 */5 * * * ?")
    public void getFileList() {
        fileStorageService.getFileInfo(FileUploadType.image.name());
        fileStorageService.getFileInfo(FileUploadType.video.name());

    }


}
