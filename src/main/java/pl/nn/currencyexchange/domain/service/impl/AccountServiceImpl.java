package pl.nn.currencyexchange.domain.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.nn.currencyexchange.application.rest.dto.AccountBalanceQuery;
import pl.nn.currencyexchange.application.rest.dto.CreateAccountCommand;
import pl.nn.currencyexchange.application.rest.dto.ExchangeMoneyCommand;
import pl.nn.currencyexchange.domain.entity.Account;
import pl.nn.currencyexchange.domain.entity.CurrencyBalance;
import pl.nn.currencyexchange.domain.entity.Owner;
import pl.nn.currencyexchange.domain.enums.Currency;
import pl.nn.currencyexchange.domain.exception.AccountNotExistsException;
import pl.nn.currencyexchange.domain.exception.CurrencyExchangeRatesNotFoundException;
import pl.nn.currencyexchange.domain.exception.NotEnoughFundsException;
import pl.nn.currencyexchange.domain.mapper.AccountMapper;
import pl.nn.currencyexchange.domain.model.ExchangeRates;
import pl.nn.currencyexchange.domain.repository.AccountRepository;
import pl.nn.currencyexchange.domain.service.AccountService;
import pl.nn.currencyexchange.infrastucture.client.NbpClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

  private static final String ACCOUNT_NOT_FOUND_ERROR = "Unable to found account with id [%s]";
  private static final String EXCHANGE_NOT_FOUND_ERROR =
      "Unable to resolve exchange rates for currency [%s]";
  private static final String EXCHANGE_NOT_POSSIBLE_ERROR =
      "Unable to exchange for target currency, required amount: [%s], actual value: [%s]";

  private final AccountRepository accountRepository;
  private final AccountMapper accountMapper;
  private final NbpClient nbpClient;

  @Override
  public UUID createNewAccount(CreateAccountCommand command) {
    Account account = new Account();
    account.setOwner(createOwner(command));
    account.setWallet(createInitialWallet(account, command.getBalance()));
    var createdAccount = accountRepository.save(account);
    return createdAccount.getId();
  }

  @Override
  public AccountBalanceQuery getAccountBalance(UUID accountId) {
    var account = accountRepository.findById(accountId).orElseThrow(() ->
        new AccountNotExistsException(String.format(ACCOUNT_NOT_FOUND_ERROR, accountId)));
    return accountMapper.map(account);
  }

  @Override
  public AccountBalanceQuery exchangeMoney(UUID accountId, ExchangeMoneyCommand command) {
    var account = accountRepository.findById(accountId).orElseThrow(() ->
        new AccountNotExistsException(String.format(ACCOUNT_NOT_FOUND_ERROR, accountId)));
    var exchangeRates = getExchangeRatesForCurrency(Currency.USD);
    var exchangedValue = getExchangedValue(command, exchangeRates);
    verifyActualWallet(account, exchangedValue, command.getCurrency());
    return exchange(account, command, exchangedValue);
  }

  private Owner createOwner(CreateAccountCommand command) {
    return Owner.builder()
        .firstName(command.getFirstName())
        .lastName(command.getLastName())
        .build();
  }

  private List<CurrencyBalance> createInitialWallet(Account account, BigDecimal balance) {
    return List.of(
        CurrencyBalance.builder()
            .account(account)
            .currency(Currency.PLN)
            .amount(balance)
            .build()
    );
  }

  private ExchangeRates.Rates getExchangeRatesForCurrency(Currency currency) {
    return Optional.ofNullable(nbpClient.getExchangeRates(currency.name()))
        .map(ExchangeRates::getRatesList)
        .orElse(Collections.emptyList()).stream()
        .findFirst()
        .orElseThrow(() -> new CurrencyExchangeRatesNotFoundException(
            String.format(EXCHANGE_NOT_FOUND_ERROR, currency)));
  }

  /**
   * Return value to subtract from current currency based on exchange rate.
   */
  private BigDecimal getExchangedValue(ExchangeMoneyCommand command,
                                       ExchangeRates.Rates exchangeRates) {
    if (command.getCurrency().equals(Currency.PLN)) {
      return command.getAmount().divide(exchangeRates.getBid(), 4, RoundingMode.HALF_UP);
    } else {
      return command.getAmount().multiply(exchangeRates.getAsk());
    }
  }

  private void verifyActualWallet(Account account, BigDecimal exchangedValue,
                                  Currency targetCurrency) {
    if (targetCurrency.equals(Currency.PLN)) {
      verifyBalance(account, exchangedValue, Currency.USD);
    } else {
      verifyBalance(account, exchangedValue, Currency.PLN);
    }
  }

  private void verifyBalance(Account account, BigDecimal exchangedValue,
                             Currency currency) {
    var currentAmount = account.getWallet().stream()
        .filter(balance -> balance.getCurrency().equals(currency))
        .findFirst()
        .map(CurrencyBalance::getAmount)
        .orElse(BigDecimal.ZERO);
    if (currentAmount.compareTo(exchangedValue) < 0) {
      throw new NotEnoughFundsException(
          String.format(EXCHANGE_NOT_POSSIBLE_ERROR, exchangedValue, currentAmount));
    }
  }

  private AccountBalanceQuery exchange(Account account, ExchangeMoneyCommand command,
                                       BigDecimal exchangedValue) {
    CurrencyBalance sourceBalance;
    CurrencyBalance targetBalance;
    if (command.getCurrency().equals(Currency.PLN)) {
      sourceBalance = getBalance(account, Currency.USD);
      targetBalance = getBalance(account, Currency.PLN);
    } else {
      sourceBalance = getBalance(account, Currency.PLN);
      targetBalance = getBalance(account, Currency.USD);
    }
    sourceBalance.setAmount(sourceBalance.getAmount().subtract(exchangedValue));
    targetBalance.setAmount(targetBalance.getAmount().add(command.getAmount()));
    if (targetBalance.getId() == null) {
      account.getWallet().add(targetBalance);
    }
    var saved = accountRepository.save(account);
    return accountMapper.map(saved);
  }

  private CurrencyBalance getBalance(Account account, Currency currency) {
    return account.getWallet().stream()
        .filter(balance -> balance.getCurrency().equals(currency))
        .findFirst()
        .orElse(createEmptyCurrencyBalance(account, currency));
  }

  private CurrencyBalance createEmptyCurrencyBalance(Account account, Currency currency) {
    return CurrencyBalance.builder()
        .account(account)
        .currency(currency)
        .amount(BigDecimal.ZERO)
        .build();
  }
}
