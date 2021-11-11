package top.keiskeiframework.file.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> implements Serializable {
    private static final long serialVersionUID = -5894814888679559266L;
    private int total;
    private List<T> data;

}
