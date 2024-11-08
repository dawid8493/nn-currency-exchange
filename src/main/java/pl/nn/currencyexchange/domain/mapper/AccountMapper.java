package pl.nn.currencyexchange.domain.mapper;

import org.mapstruct.Mapper;
import pl.nn.currencyexchange.application.rest.dto.AccountBalanceQuery;
import pl.nn.currencyexchange.domain.entity.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {

  AccountBalanceQuery map(Account account);
}
