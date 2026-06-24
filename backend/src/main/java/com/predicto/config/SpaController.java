package com.predicto.config;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/",
        "/{path:^(?!api|assets|actuator|test-static).*$}/**"
    })
    public String forward() {
        return "forward:/index.html";
    }

    @GetMapping("/test-static")
    @ResponseBody
    public String testStatic() {
        try {
            var resource = new ClassPathResource("/static/index.html");
            return "index.html EXISTS: " + resource.exists() + " READABLE: " + resource.isReadable() + " URL: " + resource.getURL();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
