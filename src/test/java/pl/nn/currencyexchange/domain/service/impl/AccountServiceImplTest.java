package pl.nn.currencyexchange.domain.service.impl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import pl.nn.currencyexchange.domain.mapper.AccountMapperImpl;
import pl.nn.currencyexchange.domain.model.ExchangeRates;
import pl.nn.currencyexchange.domain.repository.AccountRepository;
import pl.nn.currencyexchange.domain.service.AccountService;
import pl.nn.currencyexchange.infrastucture.client.NbpClient;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  private static final UUID ACCOUNT_ID = UUID.randomUUID();
  private static final UUID OWNER_ID = UUID.randomUUID();
  private static final UUID BALANCE_ID = UUID.randomUUID();
  private static final UUID BALANCE_USD_ID = UUID.randomUUID();

  private static final String FIRST_NAME = "firstName";
  private static final String LAST_NAME = "lastName";

  private static final BigDecimal INITIAL_BALANCE = new BigDecimal("1000.00");

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private NbpClient nbpClient;

  private final AccountMapper accountMapper = new AccountMapperImpl();

  private AccountService accountService;


  @BeforeEach
  public void init() {
    accountService = new AccountServiceImpl(accountRepository, accountMapper, nbpClient);
  }

  @Test
  void shouldCreateNewAccount() {
    // given
    var command = CreateAccountCommand.builder()
        .firstName(FIRST_NAME)
        .lastName(LAST_NAME)
        .balance(INITIAL_BALANCE)
        .build();

    var expectedAccount = prepareMockedAccount();

    when(accountRepository.save(any()))
        .thenReturn(expectedAccount);

    // when
    var result = accountService.createNewAccount(command);

    // then
    assertThat(result).isEqualTo(expectedAccount.getId());
  }

  @Test
  void shouldSuccessfullyGetAccountBalanceForExistingAccount() {
    // given
    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.of(prepareMockedAccount()));

    var expectedResult = AccountBalanceQuery.builder()
        .owner(AccountBalanceQuery.Owner.builder()
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build())
        .wallet(List.of(
            AccountBalanceQuery.CurrencyBalance.builder()
                .amount(INITIAL_BALANCE)
                .currency(Currency.PLN)
                .build()
        ))
        .build();

    // when
    var result = accountService.getAccountBalance(ACCOUNT_ID);

    // then
    assertThat(result).isEqualTo(expectedResult);
  }

  @Test
  void shouldThrowExceptionForGetAccountBalanceForNonExistingAccount() {
    // given
    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.empty());

    // when
    Throwable thrown = catchThrowable(() -> accountService.getAccountBalance(ACCOUNT_ID));

    // then
    assertThat(thrown).isInstanceOf(AccountNotExistsException.class);
  }

  @Test
  void shouldSuccessfullyBuyUsdCurrency() {
    // given
    var command = ExchangeMoneyCommand.builder()
        .amount(new BigDecimal("10.00"))
        .currency(Currency.USD)
        .build();

    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.of(prepareMockedAccount()));

    when(nbpClient.getExchangeRates(eq(Currency.USD.name())))
        .thenReturn(prepareMockedExchangeRates());

    when(accountRepository.save(any()))
        .thenReturn(prepareMockedAccountAfterExchangeMoney(
            new BigDecimal("959.69"),
            new BigDecimal("10.00")
        ));

    // when
    var result = accountService.exchangeMoney(ACCOUNT_ID, command);

    // then
    var plnWallet = result.getWallet().stream()
        .filter(d -> d.getCurrency().equals(Currency.PLN))
        .findFirst().get();

    var usdWallet = result.getWallet().stream()
        .filter(d -> d.getCurrency().equals(Currency.USD))
        .findFirst().get();

    assertThat(plnWallet.getAmount()).isEqualTo(new BigDecimal("959.69"));
    assertThat(usdWallet.getAmount()).isEqualTo(new BigDecimal("10.00"));
  }

  @Test
  void shouldSuccessfullySellUsdCurrency() {
    // given
    var command = ExchangeMoneyCommand.builder()
        .amount(new BigDecimal("50.00"))
        .currency(Currency.PLN)
        .build();

    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.of(prepareMockedAccountWithUsd()));

    when(nbpClient.getExchangeRates(eq(Currency.USD.name())))
        .thenReturn(prepareMockedExchangeRates());

    when(accountRepository.save(any()))
        .thenReturn(prepareMockedAccountAfterExchangeMoney(
            new BigDecimal("1050.00"),
            new BigDecimal("17.35")
        ));

    // when
    var result = accountService.exchangeMoney(ACCOUNT_ID, command);

    // then
    var plnWallet = result.getWallet().stream()
        .filter(d -> d.getCurrency().equals(Currency.PLN))
        .findFirst().get();

    var usdWallet = result.getWallet().stream()
        .filter(d -> d.getCurrency().equals(Currency.USD))
        .findFirst().get();

    assertThat(plnWallet.getAmount()).isEqualTo(new BigDecimal("1050.00"));
    assertThat(usdWallet.getAmount()).isEqualTo(new BigDecimal("17.35"));
  }

  @Test
  void shouldThrowExceptionForExchangeCurrencyWhenGivenAccountDoesNotExist() {
    // given
    var command = ExchangeMoneyCommand.builder()
        .amount(new BigDecimal("10.00"))
        .currency(Currency.USD)
        .build();

    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.empty());

    // when
    Throwable thrown = catchThrowable(() -> accountService.exchangeMoney(ACCOUNT_ID, command));

    // then
    assertThat(thrown).isInstanceOf(AccountNotExistsException.class);
  }

  @Test
  void shouldThrowExceptionForExchangeCurrencyWhenUserHasNotEnoughBalance() {
    // given
    var command = ExchangeMoneyCommand.builder()
        .amount(new BigDecimal("1000.00"))
        .currency(Currency.USD)
        .build();

    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.of(prepareMockedAccountWithUsd()));

    when(nbpClient.getExchangeRates(eq(Currency.USD.name())))
        .thenReturn(prepareMockedExchangeRates());

    // when
    Throwable thrown = catchThrowable(() -> accountService.exchangeMoney(ACCOUNT_ID, command));

    // then
    assertThat(thrown).isInstanceOf(NotEnoughFundsException.class);
  }

  @Test
  void shouldThrowExceptionForExchangeUnknownCurrency() {
    // given
    var command = ExchangeMoneyCommand.builder()
        .amount(new BigDecimal("1000.00"))
        .currency(Currency.USD)
        .build();

    when(accountRepository.findById(eq(ACCOUNT_ID)))
        .thenReturn(Optional.of(prepareMockedAccountWithUsd()));

    when(nbpClient.getExchangeRates(eq(Currency.USD.name())))
        .thenReturn(null);

    // when
    Throwable thrown = catchThrowable(() -> accountService.exchangeMoney(ACCOUNT_ID, command));

    // then
    assertThat(thrown).isInstanceOf(CurrencyExchangeRatesNotFoundException.class);
  }

  private Account prepareMockedAccount() {
    var wallet = new ArrayList<CurrencyBalance>();
    wallet.add(CurrencyBalance.builder()
        .id(BALANCE_ID)
        .amount(INITIAL_BALANCE)
        .currency(Currency.PLN)
        .build());

    return Account.builder()
        .id(ACCOUNT_ID)
        .owner(Owner.builder()
            .id(OWNER_ID)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build())
        .wallet(wallet)
        .build();
  }

  private Account prepareMockedAccountWithUsd() {
    return Account.builder()
        .id(ACCOUNT_ID)
        .owner(Owner.builder()
            .id(OWNER_ID)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build())
        .wallet(List.of(
            CurrencyBalance.builder()
                .id(BALANCE_ID)
                .amount(new BigDecimal("1000.00"))
                .currency(Currency.PLN)
                .build(),
            CurrencyBalance.builder()
                .id(BALANCE_USD_ID)
                .amount(new BigDecimal("30.00"))
                .currency(Currency.USD)
                .build()
        ))
        .build();
  }

  private Account prepareMockedAccountAfterExchangeMoney(BigDecimal plnAmount,
                                                         BigDecimal usdAmount) {
    return Account.builder()
        .id(ACCOUNT_ID)
        .owner(Owner.builder()
            .id(OWNER_ID)
            .firstName(FIRST_NAME)
            .lastName(LAST_NAME)
            .build())
        .wallet(List.of(
            CurrencyBalance.builder()
                .id(BALANCE_ID)
                .amount(plnAmount)
                .currency(Currency.PLN)
                .build(),
            CurrencyBalance.builder()
                .id(BALANCE_USD_ID)
                .amount(usdAmount)
                .currency(Currency.USD)
                .build()
        ))
        .build();
  }

  private ExchangeRates prepareMockedExchangeRates() {
    return ExchangeRates.builder()
        .code(Currency.USD.name())
        .ratesList(List.of(
            ExchangeRates.Rates.builder()
                .effectiveDate(LocalDate.now())
                .ask(new BigDecimal("4.0311"))
                .bid(new BigDecimal("3.9513"))
                .build()
        ))
        .build();
  }
}