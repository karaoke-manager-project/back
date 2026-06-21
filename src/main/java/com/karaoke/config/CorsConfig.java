package com.karaoke.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
  @Value("${FRONT_URL}")
  private String frontUrl;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(frontUrl)
        .allowedMethods("*")
        .allowedHeaders("*")
        .allowCredentials(true);
  }
}
