package pl.nn.currencyexchange.infrastucture.repository;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.nn.currencyexchange.domain.entity.Account;
import pl.nn.currencyexchange.domain.repository.AccountRepository;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

  private final JpaAccountRepository jpaAccountRepository;

  @Override
  public Optional<Account> findById(UUID accountId) {
    return jpaAccountRepository.findById(accountId);
  }

  @Override
  public Account save(Account account) {
    return jpaAccountRepository.save(account);
  }
}
