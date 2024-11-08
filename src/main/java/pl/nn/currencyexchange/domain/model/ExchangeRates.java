package pl.nn.currencyexchange.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRates {
  
  private String code;
  
  @JsonProperty("rates")
  private List<Rates> ratesList;

  @Data
  @Builder
  public static class Rates {
    
    private LocalDate effectiveDate;
    private BigDecimal bid;
    private BigDecimal ask;
  }
}
