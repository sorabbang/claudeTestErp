package com.erp.controller;

import com.erp.domain.Account;
import com.erp.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public String list(Model model) {
        List<Account> allAccounts = accountService.findAll();
        model.addAttribute("accounts", allAccounts);

        // 유형별 그룹화 (enum 순서 유지)
        Map<Account.AccountType, List<Account>> groupedAccounts = new LinkedHashMap<>();
        for (Account.AccountType type : Account.AccountType.values()) {
            groupedAccounts.put(type, new ArrayList<>());
        }
        for (Account account : allAccounts) {
            groupedAccounts.get(account.getType()).add(account);
        }
        model.addAttribute("groupedAccounts", groupedAccounts);
        model.addAttribute("accountTypes", Account.AccountType.values());

        return "account/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("account", new Account());
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "account/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("account", accountService.findById(id));
        model.addAttribute("accountTypes", Account.AccountType.values());
        return "account/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Account account, RedirectAttributes redirectAttributes) {
        accountService.save(account);
        redirectAttributes.addFlashAttribute("message", "계정과목이 저장되었습니다.");
        return "redirect:/accounts";
    }

    @GetMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        accountService.delete(id);
        redirectAttributes.addFlashAttribute("message", "계정과목이 삭제되었습니다.");
        return "redirect:/accounts";
    }
}
