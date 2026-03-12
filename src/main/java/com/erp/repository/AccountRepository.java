package com.erp.repository;

import com.erp.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    List<Account> findByType(Account.AccountType type);
    List<Account> findByActiveTrue();
    List<Account> findByNameContaining(String name);
    long countByType(Account.AccountType type);
    long countByActiveTrue();
}
