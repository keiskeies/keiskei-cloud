package top.keiskeiframework.file.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * <p>
 *
 * </P>
 *
 * @author CJM right_way@foxmail.com
 * @since 2021/12/8 21:36
 */
@Controller
@RequestMapping
public class IndexController {

    @GetMapping
    public String index() {
        return "index";
    }
}
