package com.nexotrade.os.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BinanceStreamServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private DummyTradePersistenceService tradePersistenceService;
    private BinanceStreamService binanceStreamService;

    // Classe dummy para substituir o Mockito (evita problemas de ByteBuddy Agent no Java 24)
    static class DummyTradePersistenceService extends TradePersistenceService {
        public boolean saved = false;
        public String lastSymbol = null;
        public BigDecimal lastPrice = null;

        public DummyTradePersistenceService() {
            super(null);
        }

        @Override
        public void saveTradeAsync(String symbol, BigDecimal price) {
            this.saved = true;
            this.lastSymbol = symbol;
            this.lastPrice = price;
        }
    }

    // Dummy WebSocketSession para evitar NullPointerException
    static class DummyWebSocketSession implements WebSocketSession {
        // Implementação vazia, retornamos nulos ou exceções, não será usado
        @Override public String getId() { return "1"; }
        @Override public java.net.URI getUri() { return null; }
        @Override public org.springframework.http.HttpHeaders getHandshakeHeaders() { return null; }
        @Override public java.util.Map<String, Object> getAttributes() { return null; }
        @Override public java.security.Principal getPrincipal() { return null; }
        @Override public java.net.InetSocketAddress getLocalAddress() { return null; }
        @Override public java.net.InetSocketAddress getRemoteAddress() { return null; }
        @Override public String getAcceptedProtocol() { return null; }
        @Override public void setTextMessageSizeLimit(int messageSizeLimit) {}
        @Override public int getTextMessageSizeLimit() { return 0; }
        @Override public void setBinaryMessageSizeLimit(int messageSizeLimit) {}
        @Override public int getBinaryMessageSizeLimit() { return 0; }
        @Override public java.util.List<org.springframework.web.socket.WebSocketExtension> getExtensions() { return null; }
        @Override public void sendMessage(org.springframework.web.socket.WebSocketMessage<?> message) {}
        @Override public boolean isOpen() { return true; }
        @Override public void close() {}
        @Override public void close(org.springframework.web.socket.CloseStatus status) {}
    }

    @BeforeEach
    void setUp() {
        tradePersistenceService = new DummyTradePersistenceService();
        // Não passamos webSocketClient, pois não testaremos a conexão real
        binanceStreamService = new BinanceStreamService(null, objectMapper, tradePersistenceService);
    }

    @Test
    void testHandleMessage_WithValidJson_ShouldSaveTrade() throws Exception {
        String jsonPayload = "{\"e\":\"trade\",\"p\":\"65000.50\",\"q\":\"0.001\"}";
        TextMessage message = new TextMessage(jsonPayload);

        binanceStreamService.handleMessage(new DummyWebSocketSession(), message);

        assertTrue(tradePersistenceService.saved, "O trade deveria ter sido salvo");
        assertEquals("BTCUSDT", tradePersistenceService.lastSymbol);
        assertEquals(new BigDecimal("65000.50"), tradePersistenceService.lastPrice);
    }

    @Test
    void testHandleMessage_WithInvalidJson_ShouldNotCrash() throws Exception {
        String invalidJson = "invalid json {";
        TextMessage message = new TextMessage(invalidJson);

        assertDoesNotThrow(() -> {
            binanceStreamService.handleMessage(new DummyWebSocketSession(), message);
        });

        assertFalse(tradePersistenceService.saved, "Nenhum trade deveria ser salvo para JSON inválido");
    }

    @Test
    void testHandleMessage_WithMissingPrice_ShouldNotCrash() throws Exception {
        String jsonPayload = "{\"e\":\"trade\"}";
        TextMessage message = new TextMessage(jsonPayload);

        assertDoesNotThrow(() -> {
            binanceStreamService.handleMessage(new DummyWebSocketSession(), message);
        });

        assertFalse(tradePersistenceService.saved, "Nenhum trade deveria ser salvo se não houver o preço (p)");
    }
}
