package com.example.transaction.model;

import java.math.BigDecimal;

public class TransactionMessage {
    private String settlementNo;  // 核算流水号
    private String channel;       // 交易渠道
    private BigDecimal amount;    // 交易金额
    private String userId;        // 用户ID

    // Getters and Setters
    public String getSettlementNo() {
        return settlementNo;
    }

    public void setSettlementNo(String settlementNo) {
        this.settlementNo = settlementNo;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "TransactionMessage{" +
                "settlementNo='" + settlementNo + '\'' +
                ", channel='" + channel + '\'' +
                ", amount=" + amount +
                ", userId='" + userId + '\'' +
                '}';
    }
} 