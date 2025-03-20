package com.example.transaction.scheduler;

import com.example.transaction.model.TransactionMessage;
import com.example.transaction.mq.TransactionProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@EnableScheduling
public class TransactionScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionScheduler.class);
    
    @Autowired
    private TransactionProducer transactionProducer;
    
    /**
     * 模拟上游系统每天发送批量扣款消息
     * 每30秒执行一次，用于演示
     */
    @Scheduled(fixedRate = 30000)
    public void simulateUpstreamBatchDeduction() {
        log.info("Starting simulated batch deduction...");
        
        try {
            // 模拟生成3条交易消息
            for (int i = 0; i < 3; i++) {
                TransactionMessage message = new TransactionMessage();
                message.setSettlementNo(UUID.randomUUID().toString());
                message.setChannel("BANK_" + (i % 3 + 1));
                message.setAmount(new BigDecimal("100.00").add(new BigDecimal(i * 10)));
                message.setUserId("USER_" + (1000 + i));
                
                // 发送到MQ
                transactionProducer.sendTransactionMessage(message);
                
                log.info("Simulated transaction message created: {}", message);
            }
            
            // 模拟重复消息（用于测试幂等性）
            if (Math.random() > 0.5) {
                TransactionMessage duplicateMessage = new TransactionMessage();
                duplicateMessage.setSettlementNo("FIXED_SETTLEMENT_NO");
                duplicateMessage.setChannel("BANK_DUPLICATE");
                duplicateMessage.setAmount(new BigDecimal("999.99"));
                duplicateMessage.setUserId("USER_DUPLICATE");
                
                transactionProducer.sendTransactionMessage(duplicateMessage);
                
                log.info("Simulated duplicate transaction message sent: {}", duplicateMessage);
            }
            
            log.info("Simulated batch deduction completed");
        } catch (Exception e) {
            log.error("Failed to simulate upstream batch deduction", e);
        }
    }
} 