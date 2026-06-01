package com.nexotrade.os.service;

import com.nexotrade.os.model.OrderType;
import com.nexotrade.os.model.TradeOrder;
import com.nexotrade.os.model.Wallet;
import com.nexotrade.os.repository.TradeOrderRepository;
import com.nexotrade.os.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
public class TradingEngineService {

    private final WalletRepository walletRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final BinanceStreamService binanceStreamService;

    public TradingEngineService(WalletRepository walletRepository, TradeOrderRepository tradeOrderRepository, BinanceStreamService binanceStreamService) {
        this.walletRepository = walletRepository;
        this.tradeOrderRepository = tradeOrderRepository;
        this.binanceStreamService = binanceStreamService;
    }

    @Transactional
    public TradeOrder executeBuy(BigDecimal amountUsd) {
        BigDecimal currentPrice = binanceStreamService.getLastPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Preço atual do BTC indisponível. Aguarde o Radar.");
        }

        Wallet wallet = walletRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Carteira não encontrada."));

        if (wallet.getUsdBalance().compareTo(amountUsd) < 0) {
            throw new IllegalArgumentException("Saldo USD insuficiente.");
        }

        // Calcula a fração de BTC a ser comprada
        BigDecimal amountBtc = amountUsd.divide(currentPrice, 8, RoundingMode.HALF_DOWN);

        // Atualiza saldos
        wallet.setUsdBalance(wallet.getUsdBalance().subtract(amountUsd));
        wallet.setBtcBalance(wallet.getBtcBalance().add(amountBtc));
        walletRepository.save(wallet);

        // Registra ordem
        TradeOrder order = new TradeOrder(OrderType.BUY, amountBtc, currentPrice, amountUsd, Instant.now());
        return tradeOrderRepository.save(order);
    }

    @Transactional
    public TradeOrder executeSell(BigDecimal amountBtc) {
        BigDecimal currentPrice = binanceStreamService.getLastPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Preço atual do BTC indisponível. Aguarde o Radar.");
        }

        Wallet wallet = walletRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Carteira não encontrada."));

        if (wallet.getBtcBalance().compareTo(amountBtc) < 0) {
            throw new IllegalArgumentException("Saldo BTC insuficiente.");
        }

        // Calcula o valor em USD a ser recebido
        BigDecimal totalUsd = amountBtc.multiply(currentPrice).setScale(2, RoundingMode.HALF_DOWN);

        // Atualiza saldos
        wallet.setBtcBalance(wallet.getBtcBalance().subtract(amountBtc));
        wallet.setUsdBalance(wallet.getUsdBalance().add(totalUsd));
        walletRepository.save(wallet);

        // Registra ordem
        TradeOrder order = new TradeOrder(OrderType.SELL, amountBtc, currentPrice, totalUsd, Instant.now());
        return tradeOrderRepository.save(order);
    }
}
