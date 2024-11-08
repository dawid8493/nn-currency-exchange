package pl.nn.currencyexchange.domain.repository;

import java.util.Optional;
import java.util.UUID;
import pl.nn.currencyexchange.domain.entity.Account;

public interface AccountRepository {

  Optional<Account> findById(UUID accountId);

  Account save(Account account);
}
