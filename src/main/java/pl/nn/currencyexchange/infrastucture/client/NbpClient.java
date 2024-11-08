package pl.nn.currencyexchange.infrastucture.client;

import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.nn.currencyexchange.domain.model.ExchangeRates;
import pl.nn.currencyexchange.infrastucture.configuration.feign.FeignClientConfig;

@FeignClient(name = "nbp-client", url = "${feign.nbp.url}", configuration = FeignClientConfig.class)
public interface NbpClient {

  @GetMapping("/exchangerates/rates/C/{currency}")
  @Headers("Accept: application/json")
  ExchangeRates getExchangeRates(@RequestParam("currency") String currency);
}
