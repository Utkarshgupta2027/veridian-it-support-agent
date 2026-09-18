package com.veridian.agent.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.*;
@Configuration
public class CorsConfig {
 @Bean WebMvcConfigurer cors(@Value("${app.cors.allowed-origins}") String origins){
  return new WebMvcConfigurer(){ public void addCorsMappings(CorsRegistry r){
   r.addMapping("/api/**").allowedOrigins(origins.split(","))
    .allowedMethods("GET","POST","PUT","OPTIONS").allowedHeaders("*");
  }};
 }
}
