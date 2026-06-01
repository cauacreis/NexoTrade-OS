package com.nexotrade.os.service;

import com.nexotrade.os.model.TradeRecord;
import com.nexotrade.os.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TradePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(TradePersistenceService.class);
    private final TradeRepository tradeRepository;

    public TradePersistenceService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Async
    public void saveTradeAsync(String symbol, BigDecimal price) {
        try {
            TradeRecord record = new TradeRecord(symbol, price, Instant.now());
            tradeRepository.save(record);
        } catch (Exception e) {
            log.error("Erro ao persistir trade assincronamente: {}", e.getMessage(), e);
        }
    }
}
