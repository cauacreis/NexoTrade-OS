package com.nexotrade.os.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trade_orders")
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private BigDecimal amountBtc;
    private BigDecimal priceExecuted;
    private BigDecimal totalUsd;
    private Instant timestamp;

    public TradeOrder() {
    }

    public TradeOrder(OrderType type, BigDecimal amountBtc, BigDecimal priceExecuted, BigDecimal totalUsd, Instant timestamp) {
        this.type = type;
        this.amountBtc = amountBtc;
        this.priceExecuted = priceExecuted;
        this.totalUsd = totalUsd;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public BigDecimal getAmountBtc() {
        return amountBtc;
    }

    public void setAmountBtc(BigDecimal amountBtc) {
        this.amountBtc = amountBtc;
    }

    public BigDecimal getPriceExecuted() {
        return priceExecuted;
    }

    public void setPriceExecuted(BigDecimal priceExecuted) {
        this.priceExecuted = priceExecuted;
    }

    public BigDecimal getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(BigDecimal totalUsd) {
        this.totalUsd = totalUsd;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
