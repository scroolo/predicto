package com.predicto.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.mediaType("css", MediaType.valueOf("text/css"))
                  .mediaType("js", MediaType.valueOf("application/javascript"))
                  .mediaType("svg", MediaType.valueOf("image/svg+xml"));
    }
}
