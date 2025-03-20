package com.example.transaction.mq;

import com.example.transaction.model.TransactionMessage;
import com.example.transaction.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@Component
public class TransactionConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);
    
    @Autowired
    private TransactionService transactionService;
    
    @Value("${rocketmq.name-server}")
    private String nameServer;
    
    @Value("${rocketmq.topic}")
    private String topic;
    
    @Value("${rocketmq.consumer-group}")
    private String consumerGroup;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private DefaultMQPushConsumer consumer;
    
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing RocketMQ consumer with nameServer: {}", nameServer);
            
            consumer = new DefaultMQPushConsumer(consumerGroup);
            consumer.setNamesrvAddr(nameServer);
            consumer.subscribe(topic, "*");
            
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                    for (MessageExt msg : msgs) {
                        log.info("Received message, messageId={}", msg.getMsgId());
                        
                        try {
                            boolean result = transactionService.processTransaction(msg.getBody());
                            
                            if (result) {
                                log.info("Transaction processed successfully, messageId={}", msg.getMsgId());
                            } else {
                                log.info("Transaction already processed or failed, messageId={}", msg.getMsgId());
                            }
                        } catch (Exception e) {
                            log.error("Failed to process message, messageId={}", msg.getMsgId(), e);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            
            consumer.start();
            log.info("RocketMQ consumer started successfully");
        } catch (Exception e) {
            log.error("Failed to start RocketMQ consumer: {}", e.getMessage(), e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        if (consumer != null) {
            consumer.shutdown();
            log.info("RocketMQ consumer shutdown successfully");
        }
    }
}