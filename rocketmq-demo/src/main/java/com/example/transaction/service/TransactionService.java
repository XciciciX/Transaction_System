package com.example.transaction.service;

import com.example.transaction.model.TransactionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionService {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 处理交易消息
     * @param messageBody 交易消息体
     * @return 处理结果
     */
    public boolean processTransaction(byte[] messageBody) {
        try {
            String messageStr = new String(messageBody);
            TransactionMessage message = objectMapper.readValue(messageStr, TransactionMessage.class);
            return processTransaction(message);
        } catch (Exception e) {
            log.error("Failed to process transaction message", e);
            return false;
        }
    }

    public boolean processTransaction(TransactionMessage message) {
        try {
            log.info("Processing transaction: {}", message);

            // 检查交易是否已存在（幂等性控制）
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transaction_main WHERE settlement_no = ?",
                Integer.class,
                message.getSettlementNo()
            );

            if (count != null && count > 0) {
                log.info("Transaction already exists: {}", message.getSettlementNo());
                return false;
            }

            // 插入交易记录
            jdbcTemplate.update(
                "INSERT INTO transaction_main (settlement_no, channel, amount, user_id, status) VALUES (?, ?, ?, ?, ?)",
                message.getSettlementNo(),
                message.getChannel(),
                message.getAmount(),
                message.getUserId(),
                "SUCCESS"
            );

            log.info("Transaction saved successfully: {}", message.getSettlementNo());
            return true;
        } catch (Exception e) {
            log.error("Failed to process transaction", e);
            return false;
        }
    }
} 