package pl.nn.currencyexchange.infrastucture.configuration.feign;

import static java.util.concurrent.TimeUnit.SECONDS;

import feign.Feign;
import feign.Logger;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

  @Bean
  public Feign.Builder feignBuilder() {
    return Feign.builder()
        .errorDecoder(new FeignErrorDecoder())
        .retryer(retryer())
        .logLevel(Logger.Level.BASIC);
  }

  @Bean
  public Retryer retryer() {
    return new Retryer.Default(SECONDS.toMillis(5), SECONDS.toMillis(20), 3) {
    };
  }

  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.BASIC;
  }
}
