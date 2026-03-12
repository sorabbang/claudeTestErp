package com.erp.controller;

import com.erp.repository.AccountRepository;
import com.erp.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public String debug(Model model) {
        // 시스템 정보
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMX = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMX = ManagementFactory.getMemoryMXBean();

        Map<String, Object> systemInfo = new LinkedHashMap<>();
        systemInfo.put("Java 버전", System.getProperty("java.version"));
        systemInfo.put("JVM", runtimeMX.getVmName() + " " + runtimeMX.getVmVersion());
        systemInfo.put("OS", System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        systemInfo.put("가용 프로세서", runtime.availableProcessors() + "개");

        long uptime = runtimeMX.getUptime();
        Duration d = Duration.ofMillis(uptime);
        systemInfo.put("서버 가동시간", String.format("%d시간 %d분 %d초", d.toHours(), d.toMinutesPart(), d.toSecondsPart()));
        systemInfo.put("현재 시각", LocalDateTime.now().toString());
        model.addAttribute("systemInfo", systemInfo);

        // 메모리 정보
        long maxMem = runtime.maxMemory() / (1024 * 1024);
        long totalMem = runtime.totalMemory() / (1024 * 1024);
        long freeMem = runtime.freeMemory() / (1024 * 1024);
        long usedMem = totalMem - freeMem;
        int memPercent = (int) ((usedMem * 100) / maxMem);

        model.addAttribute("maxMemory", maxMem);
        model.addAttribute("totalMemory", totalMem);
        model.addAttribute("usedMemory", usedMem);
        model.addAttribute("freeMemory", freeMem);
        model.addAttribute("memoryPercent", memPercent);

        // 데이터 현황
        Map<String, Long> dataCounts = new LinkedHashMap<>();
        dataCounts.put("계정과목 (accounts)", accountRepository.count());
        dataCounts.put("전표 (journals)", journalRepository.count());

        try {
            Long entryCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM journal_entries", Long.class);
            dataCounts.put("분개항목 (journal_entries)", entryCount != null ? entryCount : 0L);
        } catch (Exception e) {
            dataCounts.put("분개항목 (journal_entries)", -1L);
        }
        model.addAttribute("dataCounts", dataCounts);

        // 테이블 목록
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'");
        model.addAttribute("tables", tables);

        // DB 정보
        Map<String, String> dbInfo = new LinkedHashMap<>();
        dbInfo.put("JDBC URL", "jdbc:h2:mem:erpdb");
        dbInfo.put("Driver", "org.h2.Driver");
        dbInfo.put("Hibernate DDL", "update");
        dbInfo.put("Show SQL", "true");
        model.addAttribute("dbInfo", dbInfo);

        return "debug/index";
    }

    @PostMapping("/query")
    @ResponseBody
    public Map<String, Object> executeQuery(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new LinkedHashMap<>();
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
            result.put("error", "SQL을 입력하세요.");
            return result;
        }

        sql = sql.trim();

        // SELECT만 허용 (안전장치)
        if (!sql.toUpperCase().startsWith("SELECT") && !sql.toUpperCase().startsWith("SHOW")
                && !sql.toUpperCase().startsWith("DESCRIBE")) {
            result.put("error", "SELECT / SHOW / DESCRIBE 쿼리만 실행할 수 있습니다.");
            return result;
        }

        try {
            long start = System.currentTimeMillis();
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            long elapsed = System.currentTimeMillis() - start;

            result.put("success", true);
            result.put("rows", rows);
            result.put("rowCount", rows.size());
            result.put("elapsed", elapsed + "ms");

            if (!rows.isEmpty()) {
                result.put("columns", new ArrayList<>(rows.get(0).keySet()));
            }
        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }

    @PostMapping("/gc")
    @ResponseBody
    public Map<String, Object> runGc() {
        long beforeFree = Runtime.getRuntime().freeMemory();
        System.gc();
        long afterFree = Runtime.getRuntime().freeMemory();
        long freed = (afterFree - beforeFree) / (1024 * 1024);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("freedMB", freed);
        result.put("message", "GC 실행 완료 (" + freed + "MB 해제)");
        return result;
    }
}
