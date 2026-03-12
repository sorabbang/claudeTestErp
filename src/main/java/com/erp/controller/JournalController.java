package com.erp.controller;

import com.erp.domain.Journal;
import com.erp.domain.JournalEntry;
import com.erp.dto.JournalFormDto;
import com.erp.service.AccountService;
import com.erp.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;
    private final AccountService accountService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("journals", journalService.findAll());
        return "journal/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        JournalFormDto form = new JournalFormDto();
        model.addAttribute("journalForm", form);
        model.addAttribute("accounts", accountService.findActiveAccounts());
        model.addAttribute("statuses", Journal.JournalStatus.values());
        return "journal/form";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Journal journal = journalService.findById(id);
        model.addAttribute("journal", journal);

        BigDecimal totalDebit = journal.getEntries().stream()
                .map(JournalEntry::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = journal.getEntries().stream()
                .map(JournalEntry::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("totalCredit", totalCredit);

        return "journal/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Journal journal = journalService.findById(id);
        JournalFormDto form = JournalFormDto.fromEntity(journal);
        model.addAttribute("journalForm", form);
        model.addAttribute("accounts", accountService.findActiveAccounts());
        model.addAttribute("statuses", Journal.JournalStatus.values());
        return "journal/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute JournalFormDto journalForm, RedirectAttributes redirectAttributes) {
        // 빈 분개 라인 제거
        journalForm.getEntries().removeIf(e -> e.getAccountId() == null);

        if (journalForm.getEntries().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "최소 1개 이상의 분개 라인을 입력해주세요.");
            return "redirect:/journals/new";
        }

        if (!journalForm.isBalanced()) {
            redirectAttributes.addFlashAttribute("error",
                    "차변과 대변의 합계가 일치하지 않습니다. (차변: " +
                    journalForm.getTotalDebit() + ", 대변: " + journalForm.getTotalCredit() + ")");
            return "redirect:/journals/new";
        }

        Journal saved = journalService.saveFromDto(journalForm);
        redirectAttributes.addFlashAttribute("message", "전표 [" + saved.getVoucherNo() + "]가 저장되었습니다.");
        return "redirect:/journals/" + saved.getId();
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        journalService.delete(id);
        redirectAttributes.addFlashAttribute("message", "전표가 삭제되었습니다.");
        return "redirect:/journals";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Journal journal = journalService.updateStatus(id, Journal.JournalStatus.APPROVED);
        redirectAttributes.addFlashAttribute("message", "전표 [" + journal.getVoucherNo() + "]가 승인되었습니다.");
        return "redirect:/journals/" + id;
    }

    @PostMapping("/{id}/post")
    public String post(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Journal journal = journalService.updateStatus(id, Journal.JournalStatus.POSTED);
        redirectAttributes.addFlashAttribute("message", "전표 [" + journal.getVoucherNo() + "]가 전기 처리되었습니다.");
        return "redirect:/journals/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Journal journal = journalService.updateStatus(id, Journal.JournalStatus.CANCELLED);
        redirectAttributes.addFlashAttribute("message", "전표 [" + journal.getVoucherNo() + "]가 취소되었습니다.");
        return "redirect:/journals/" + id;
    }
}
