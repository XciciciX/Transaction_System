package com.example.transaction.mq;

import com.example.transaction.model.TransactionMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class TransactionProducer {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionProducer.class);
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.topic}")
    private String topic;
    
    @Value("${rocketmq.producer-group}")
    private String producerGroup;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private DefaultMQProducer producer;
    
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing RocketMQ producer with nameServer: {}", nameServer);
            
            producer = new DefaultMQProducer(producerGroup);
            producer.setNamesrvAddr(nameServer);
            producer.start();
            
            log.info("RocketMQ producer initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize RocketMQ producer: {}", e.getMessage(), e);
        }
    }
    
    public boolean sendTransactionMessage(TransactionMessage transactionMessage) {
        try {
            String messageBody = objectMapper.writeValueAsString(transactionMessage);
            
            Message message = new Message(
                topic,
                "transaction",
                transactionMessage.getSettlementNo(),
                messageBody.getBytes()
            );
            
            SendResult sendResult = producer.send(message);
            log.info("Transaction message sent successfully, messageId={}, settlementNo={}", 
                sendResult.getMsgId(), transactionMessage.getSettlementNo());
            
            return true;
        } catch (Exception e) {
            log.error("Failed to send transaction message, settlementNo={}", 
                transactionMessage.getSettlementNo(), e);
            return false;
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (producer != null) {
            producer.shutdown();
            log.info("RocketMQ producer shutdown successfully");
        }
    }
} 