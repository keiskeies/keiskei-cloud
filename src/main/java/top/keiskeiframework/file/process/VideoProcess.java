package top.keiskeiframework.file.process;

import lombok.Data;
import top.keiskeiframework.file.process.video.Snapshot;

/**
 * @author James Chen right_way@foxmail.com
 * @since 2020/6/6 17:12
 */
@Data
public class VideoProcess {
    private Snapshot snapshot;

    public VideoProcess(String[] processes) {
        for (String process : processes) {
            String[] actions = process.split(",");
            switch (actions[0]) {
                case "snapshot" : this.snapshot = new Snapshot(actions);break;
                default: break;
            }
        }
    }
}
