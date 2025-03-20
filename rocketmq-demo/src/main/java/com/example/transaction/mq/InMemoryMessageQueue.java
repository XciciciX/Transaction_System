package com.example.transaction.mq;

import com.example.transaction.model.TransactionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Component
public class InMemoryMessageQueue {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryMessageQueue.class);
    
    private final BlockingQueue<TransactionMessage> queue = new LinkedBlockingQueue<>();
    
    public boolean sendMessage(TransactionMessage message) {
        try {
            log.info("Sending message to in-memory queue: {}", message);
            queue.put(message);
            return true;
        } catch (InterruptedException e) {
            log.error("Failed to send message to in-memory queue", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public void startConsumer(Consumer<TransactionMessage> messageConsumer) {
        new Thread(() -> {
            log.info("Starting in-memory message consumer");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TransactionMessage message = queue.take();
                    log.info("Consuming message from in-memory queue: {}", message);
                    messageConsumer.accept(message);
                } catch (InterruptedException e) {
                    log.error("In-memory queue consumer interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Error processing message from in-memory queue", e);
                }
            }
        }, "in-memory-queue-consumer").start();
    }
} 