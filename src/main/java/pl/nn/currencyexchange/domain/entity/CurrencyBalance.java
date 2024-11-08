package pl.nn.currencyexchange.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import pl.nn.currencyexchange.domain.enums.Currency;

@Entity
@Table(name = "currency_balance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CurrencyBalance {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "currency", nullable = false)
  private Currency currency;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @ToString.Exclude
  @ManyToOne
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;
}
