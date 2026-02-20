package cn.maiaimei.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new io.swagger.v3.oas.models.info.Info()
            .title("API文档")
            .version("1.0")
            .description("Spring Boot OpenAPI示例")
            .contact(new Contact()
                .name("Your Name")
                .email("your.email@example.com")));
  }

}