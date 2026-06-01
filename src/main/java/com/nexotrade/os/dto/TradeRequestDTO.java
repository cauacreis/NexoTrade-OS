package com.nexotrade.os.dto;

import com.nexotrade.os.model.OrderType;
import java.math.BigDecimal;

public class TradeRequestDTO {
    private OrderType action;
    private BigDecimal amountUsd;
    private BigDecimal amountBtc;

    public OrderType getAction() {
        return action;
    }

    public void setAction(OrderType action) {
        this.action = action;
    }

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(BigDecimal amountUsd) {
        this.amountUsd = amountUsd;
    }

    public BigDecimal getAmountBtc() {
        return amountBtc;
    }

    public void setAmountBtc(BigDecimal amountBtc) {
        this.amountBtc = amountBtc;
    }
}
