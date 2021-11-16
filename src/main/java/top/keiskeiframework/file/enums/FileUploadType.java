package top.keiskeiframework.file.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileUploadType {
    video(false),
    image(true),
    ;

    private final Boolean md5Name;

    public String path() {
        return this.name() + "/";
    }

    public String temp() {
        return this.name() + "/temp/";
    }
}
