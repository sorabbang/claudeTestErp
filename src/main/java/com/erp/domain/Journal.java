package com.erp.domain;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journals")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String voucherNo;

    @Column(nullable = false)
    private LocalDate transactionDate;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JournalStatus status;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JournalEntry> entries = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void addEntry(JournalEntry entry) {
        entries.add(entry);
        entry.setJournal(this);
    }

    public enum JournalStatus {
        DRAFT("임시저장"),
        APPROVED("승인"),
        POSTED("전기완료"),
        CANCELLED("취소");

        private final String label;

        JournalStatus(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
