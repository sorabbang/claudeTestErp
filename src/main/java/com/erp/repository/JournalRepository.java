package com.erp.repository;

import com.erp.domain.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    Optional<Journal> findByVoucherNo(String voucherNo);
    List<Journal> findByTransactionDateBetween(LocalDate start, LocalDate end);
    List<Journal> findByStatus(Journal.JournalStatus status);
    long countByStatus(Journal.JournalStatus status);
    long countByTransactionDateBetween(LocalDate start, LocalDate end);
    List<Journal> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT MAX(j.voucherNo) FROM Journal j WHERE j.voucherNo LIKE :prefix%")
    Optional<String> findMaxVoucherNoByPrefix(@Param("prefix") String prefix);
}
