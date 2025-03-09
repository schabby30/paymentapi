package com.kibit.paymentapi.repository;

import com.kibit.paymentapi.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM account WHERE account_number = ?1 FOR UPDATE")
    Optional<Account> findByAccountNumberAndLockRow(String accountNumber);

    Optional<Account> findByAccountNumber(String accountNumber);
}
