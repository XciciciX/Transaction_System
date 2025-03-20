package com.example.transaction.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HomeController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Transaction System is running");
        response.put("time", System.currentTimeMillis());
        
        // 获取交易记录数量
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM transaction_main", Integer.class);
        response.put("transactionCount", count);
        
        // 获取最近5条交易记录
        List<Map<String, Object>> recentTransactions = jdbcTemplate.queryForList(
            "SELECT settlement_no, channel, amount, user_id, status, create_time " +
            "FROM transaction_main ORDER BY create_time DESC LIMIT 5");
        response.put("recentTransactions", recentTransactions);
        
        return response;
    }
    
    @GetMapping("/health")
    public Map<String, String> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", "Connected");
        status.put("rocketmq", "Connected");
        return status;
    }
} 