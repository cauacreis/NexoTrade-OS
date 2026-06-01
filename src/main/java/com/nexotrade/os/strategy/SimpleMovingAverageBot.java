package com.nexotrade.os.strategy;

import com.nexotrade.os.model.Wallet;
import com.nexotrade.os.repository.WalletRepository;
import com.nexotrade.os.service.TradingEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class SimpleMovingAverageBot {

    private static final Logger log = LoggerFactory.getLogger(SimpleMovingAverageBot.class);
    private static final int PERIOD = 50;
    
    private final TradingEngineService tradingEngineService;
    private final WalletRepository walletRepository;
    
    private final ConcurrentLinkedDeque<BigDecimal> priceHistory = new ConcurrentLinkedDeque<>();
    
    private volatile boolean active = false;

    public SimpleMovingAverageBot(@Lazy TradingEngineService tradingEngineService, WalletRepository walletRepository) {
        this.tradingEngineService = tradingEngineService;
        this.walletRepository = walletRepository;
    }

    public void toggleBot() {
        this.active = !this.active;
        log.info("[NEXOTRADE BOT] Status do Bot alterado para: {}", this.active ? "LIGADO" : "DESLIGADO");
    }

    public boolean isActive() {
        return active;
    }

    public void processNewPrice(BigDecimal currentPrice) {
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) return;

        priceHistory.addLast(currentPrice);
        if (priceHistory.size() > PERIOD) {
            priceHistory.removeFirst();
        }

        if (!active || priceHistory.size() < PERIOD) {
            return; // Aguarda preencher o buffer ou bot ser ligado
        }

        BigDecimal sma = calculateSMA();
        
        // Regras de execução (2% de variação)
        BigDecimal dropThreshold = sma.multiply(new BigDecimal("0.98")); // Caiu 2% da média (Oportunidade)
        BigDecimal riseThreshold = sma.multiply(new BigDecimal("1.02")); // Subiu 2% da média (Lucro)

        Wallet wallet = walletRepository.findAll().stream().findFirst().orElse(null);
        if (wallet == null) return;

        try {
            if (currentPrice.compareTo(dropThreshold) <= 0) {
                // Caiu 2% - COMPRAR
                BigDecimal amountUsdToSpend = wallet.getUsdBalance().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_DOWN);
                if (amountUsdToSpend.compareTo(new BigDecimal("10.00")) > 0) { // Compra mínima
                    log.info("\033[1;35m[BOT EXECUTADO]\033[0m Oportunidade detectada! Preço ({}) caiu 2%% abaixo da SMA ({}). Comprando $ {} de BTC...", currentPrice, sma, amountUsdToSpend);
                    tradingEngineService.executeBuy(amountUsdToSpend);
                }
            } else if (currentPrice.compareTo(riseThreshold) >= 0) {
                // Subiu 2% - VENDER
                BigDecimal amountBtcToSell = wallet.getBtcBalance();
                if (amountBtcToSell.compareTo(new BigDecimal("0.0001")) > 0) { // Venda mínima
                    log.info("\033[1;35m[BOT EXECUTADO]\033[0m Lucro detectado! Preço ({}) subiu 2%% acima da SMA ({}). Vendendo todo saldo BTC ({})...", currentPrice, sma, amountBtcToSell);
                    tradingEngineService.executeSell(amountBtcToSell);
                }
            }
        } catch (Exception e) {
            log.error("[NEXOTRADE BOT] Erro ao executar ordem automática: {}", e.getMessage());
        }
    }

    private BigDecimal calculateSMA() {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal price : priceHistory) {
            sum = sum.add(price);
        }
        return sum.divide(new BigDecimal(priceHistory.size()), 2, RoundingMode.HALF_UP);
    }
}
