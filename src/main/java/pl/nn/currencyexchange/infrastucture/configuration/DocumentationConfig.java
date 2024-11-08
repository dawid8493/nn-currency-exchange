package pl.nn.currencyexchange.infrastucture.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

  @Bean
  public OpenAPI apiInfo() {
    return new OpenAPI()
        .info(new Info()
            .title("Currency Exchange APP")
            .description("Aplikacja pozwalająca na wymianę walut.")
            .version("0.1")
            .contact(new Contact().name("Dawid Dobrzański")
                .email("dawid8493@gmail.com"))
        );
  }
}
