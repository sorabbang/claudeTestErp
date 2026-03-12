package com.erp.service;

import com.erp.domain.Account;
import com.erp.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("계정과목을 찾을 수 없습니다. ID: " + id));
    }

    public List<Account> findByType(Account.AccountType type) {
        return accountRepository.findByType(type);
    }

    public List<Account> findActiveAccounts() {
        return accountRepository.findByActiveTrue();
    }

    @Transactional
    public Account save(Account account) {
        return accountRepository.save(account);
    }

    @Transactional
    public void delete(Long id) {
        accountRepository.deleteById(id);
    }
}
