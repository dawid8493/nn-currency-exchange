package pl.nn.currencyexchange.infrastucture.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.nn.currencyexchange.domain.entity.Account;

@Repository
public interface JpaAccountRepository extends JpaRepository<Account, UUID> {
}
