package com.nexotrade.os.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexotrade.os.strategy.SimpleMovingAverageBot;

@Service
public class BinanceStreamService implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(BinanceStreamService.class);

    private final WebSocketClient webSocketClient;
    private final ObjectMapper objectMapper;
    private final TradePersistenceService tradePersistenceService;
    private final SimpleMovingAverageBot bot;
    private static final String BINANCE_STREAM_URL = "wss://stream.binance.com:9443/ws/btcusdt@trade";
    private volatile java.math.BigDecimal lastPrice = java.math.BigDecimal.ZERO;
    private final java.util.concurrent.ScheduledExecutorService mockExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

    public BinanceStreamService(WebSocketClient webSocketClient, ObjectMapper objectMapper, TradePersistenceService tradePersistenceService, SimpleMovingAverageBot bot) {
        this.webSocketClient = webSocketClient;
        this.objectMapper = objectMapper;
        this.tradePersistenceService = tradePersistenceService;
        this.bot = bot;
    }

    @PostConstruct
    public void init() {
        try {
            log.info("Iniciando conexão com o Radar NexoTrade na Binance...");
            webSocketClient.execute(this, BINANCE_STREAM_URL).whenComplete((res, ex) -> {
                if (ex != null) {
                    log.error("Erro fatal ao conectar no WebSocket: {}", ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Erro ao conectar no WebSocket da Binance: {}", e.getMessage(), e);
        }
        
        // Fallback de simulação caso a Binance demore a conectar ou dê rate limit
        mockExecutor.scheduleAtFixedRate(() -> {
            if (this.lastPrice.compareTo(java.math.BigDecimal.ZERO) == 0) {
                log.warn("[NEXOTRADE RADAR] Binance indisponível/Rate Limit. Iniciando gerador de preço simulado...");
                simulatePrice();
            } else if (this.lastPrice.compareTo(java.math.BigDecimal.valueOf(1000)) < 0) {
                // Preço mockado ativo
                simulatePrice();
            }
        }, 5, 2, java.util.concurrent.TimeUnit.SECONDS);
    }
    
    private void simulatePrice() {
        double randomVariation = (Math.random() - 0.5) * 50; // Variação de -$25 a +$25
        double basePrice = this.lastPrice.compareTo(java.math.BigDecimal.ZERO) == 0 ? 67500.00 : this.lastPrice.doubleValue();
        java.math.BigDecimal mockPrice = java.math.BigDecimal.valueOf(basePrice + randomVariation);
        this.lastPrice = mockPrice;
        tradePersistenceService.saveTradeAsync("BTCUSDT_MOCK", mockPrice);
        if (bot != null) bot.processNewPrice(mockPrice);
        System.out.printf("\033[1;36m[NEXOTRADE RADAR - MOCK]\033[0m \033[1;32mBTC/USDT Simulado -> $\033[0m \033[1;33m%,.2f\033[0m%n", mockPrice.doubleValue());
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("[NEXOTRADE RADAR] Conexão estabelecida com sucesso! Escutando transações de BTC/USDT...");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            if (message instanceof TextMessage textMessage) {
                String payload = textMessage.getPayload();
                JsonNode rootNode = objectMapper.readTree(payload);
                if (rootNode != null && rootNode.has("p")) {
                    String price = rootNode.get("p").asText();
                    double priceValue = Double.parseDouble(price);
                    
                    java.math.BigDecimal currentPrice = new java.math.BigDecimal(price);
                    this.lastPrice = currentPrice;
                    
                    // Salvar no banco usando thread separada (Virtual Threads se habilitado)
                    tradePersistenceService.saveTradeAsync("BTCUSDT", currentPrice);
                    
                    // Notificar o robô da média móvel
                    if (bot != null) {
                        bot.processNewPrice(currentPrice);
                    }
                    
                    System.out.printf("\033[1;36m[NEXOTRADE RADAR]\033[0m \033[1;32mBTC/USDT Trade Executado -> $\033[0m \033[1;33m%,.2f\033[0m%n", priceValue);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao decodificar a mensagem JSON do Trade: {}", e.getMessage());
        }
    }

    public java.math.BigDecimal getLastPrice() {
        return this.lastPrice;
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[NEXOTRADE RADAR] Erro de transporte detectado: {}", exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.warn("[NEXOTRADE RADAR] Conexão com a Binance encerrada. Status: {}. Tentando reconectar em 5 segundos...", closeStatus);
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        java.util.concurrent.CompletableFuture.delayedExecutor(5, java.util.concurrent.TimeUnit.SECONDS)
            .execute(this::init);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
