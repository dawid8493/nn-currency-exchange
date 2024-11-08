package pl.nn.currencyexchange.application.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.nn.currencyexchange.application.rest.dto.AccountBalanceQuery;
import pl.nn.currencyexchange.application.rest.dto.CreateAccountCommand;
import pl.nn.currencyexchange.application.rest.dto.ExchangeMoneyCommand;
import pl.nn.currencyexchange.domain.service.AccountService;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @Operation(summary = "Utworzenie nowego konta walutowego.")
  @PostMapping("/create")
  public ResponseEntity<UUID> createAccount(@RequestBody @Valid CreateAccountCommand command) {
    return new ResponseEntity<>(accountService.createNewAccount(command), HttpStatus.CREATED);
  }

  @Operation(summary = "Pobranie danych konta wraz z saldami we wszystkich posiadanych walutach.")
  @GetMapping("/{accountId}/balance")
  public AccountBalanceQuery getAccountBalance(@PathVariable UUID accountId) {
    return accountService.getAccountBalance(accountId);
  }

  @Operation(summary = "Wymiana waluty (wymagane wskazanie waluty źródłowej i docelowej).")
  @PostMapping("/{accountId}/exchange")
  public AccountBalanceQuery exchangeMoney(@PathVariable UUID accountId,
                                           @RequestBody @Valid ExchangeMoneyCommand command) {
    return accountService.exchangeMoney(accountId, command);
  }
}
