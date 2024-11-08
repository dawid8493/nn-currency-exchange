package pl.nn.currencyexchange.domain.service;

import java.util.UUID;
import pl.nn.currencyexchange.application.rest.dto.AccountBalanceQuery;
import pl.nn.currencyexchange.application.rest.dto.CreateAccountCommand;
import pl.nn.currencyexchange.application.rest.dto.ExchangeMoneyCommand;

public interface AccountService {
  
  UUID createNewAccount(CreateAccountCommand command);

  AccountBalanceQuery getAccountBalance(UUID accountId);

  AccountBalanceQuery exchangeMoney(UUID accountId, ExchangeMoneyCommand command);
}
