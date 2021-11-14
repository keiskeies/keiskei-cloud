package top.keiskeiframework.file.enums;


public enum FileUploadType {
    video,
    image,
    ;

    public String path() {
        return this.name() + "/";
    }
    public String temp() {
        return this.name() + "/temp/";
    }
}
