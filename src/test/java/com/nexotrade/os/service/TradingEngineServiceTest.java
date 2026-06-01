package com.nexotrade.os.service;

import com.nexotrade.os.model.OrderType;
import com.nexotrade.os.model.TradeOrder;
import com.nexotrade.os.model.Wallet;
import com.nexotrade.os.repository.TradeOrderRepository;
import com.nexotrade.os.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TradingEngineServiceTest {

    // Classes dummy para substituir o Mockito no Java 24
    static class DummyWalletRepository extends WalletRepositoryStub {
        public Wallet wallet;
        public boolean saved = false;
        
        public DummyWalletRepository(Wallet wallet) {
            this.wallet = wallet;
        }
        @Override
        public List<Wallet> findAll() {
            return List.of(wallet);
        }
        @Override
        public <S extends Wallet> S save(S entity) {
            this.saved = true;
            this.wallet = entity;
            return entity;
        }
    }

    static class DummyTradeOrderRepository extends TradeOrderRepositoryStub {
        public TradeOrder lastSavedOrder;
        @Override
        public <S extends TradeOrder> S save(S entity) {
            this.lastSavedOrder = entity;
            return entity;
        }
    }

    static class DummyBinanceStreamService extends BinanceStreamService {
        public BigDecimal mockedPrice = BigDecimal.ZERO;
        public DummyBinanceStreamService() {
            super(null, null, null);
        }
        @Override
        public BigDecimal getLastPrice() {
            return mockedPrice;
        }
    }

    private DummyWalletRepository walletRepository;
    private DummyTradeOrderRepository tradeOrderRepository;
    private DummyBinanceStreamService binanceStreamService;
    private TradingEngineService tradingEngineService;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testWallet = new Wallet(new BigDecimal("10000.00"), new BigDecimal("0.00"));
        walletRepository = new DummyWalletRepository(testWallet);
        tradeOrderRepository = new DummyTradeOrderRepository();
        binanceStreamService = new DummyBinanceStreamService();
        tradingEngineService = new TradingEngineService(walletRepository, tradeOrderRepository, binanceStreamService);
    }

    @Test
    void testExecuteBuy_Success() {
        binanceStreamService.mockedPrice = new BigDecimal("50000.00");

        TradeOrder order = tradingEngineService.executeBuy(new BigDecimal("1000.00"));

        assertNotNull(order);
        assertEquals(OrderType.BUY, order.getType());
        assertEquals(new BigDecimal("50000.00"), order.getPriceExecuted());
        assertEquals(new BigDecimal("1000.00"), order.getTotalUsd());
        
        // 1000 / 50000 = 0.02
        assertEquals(new BigDecimal("0.02000000"), order.getAmountBtc());

        // Wallet balance check
        assertEquals(new BigDecimal("9000.00"), testWallet.getUsdBalance());
        assertEquals(new BigDecimal("0.02000000"), testWallet.getBtcBalance());
        assertTrue(walletRepository.saved);
    }

    @Test
    void testExecuteBuy_InsufficientFunds() {
        binanceStreamService.mockedPrice = new BigDecimal("50000.00");

        assertThrows(IllegalArgumentException.class, () -> {
            tradingEngineService.executeBuy(new BigDecimal("15000.00"));
        });
    }

    @Test
    void testExecuteSell_Success() {
        testWallet.setBtcBalance(new BigDecimal("0.50000000"));
        binanceStreamService.mockedPrice = new BigDecimal("60000.00");

        TradeOrder order = tradingEngineService.executeSell(new BigDecimal("0.10000000"));

        assertNotNull(order);
        assertEquals(OrderType.SELL, order.getType());
        
        // 0.1 * 60000 = 6000.00
        assertEquals(new BigDecimal("6000.00"), order.getTotalUsd());

        // Wallet balance check
        assertEquals(new BigDecimal("0.40000000"), testWallet.getBtcBalance());
        assertEquals(new BigDecimal("16000.00"), testWallet.getUsdBalance());
        assertTrue(walletRepository.saved);
    }
}

// Stubs base para não poluir a classe de teste principal
abstract class WalletRepositoryStub implements WalletRepository {
    public void flush(){} public <S extends Wallet> S saveAndFlush(S entity){return null;} public <S extends Wallet> java.util.List<S> saveAllAndFlush(Iterable<S> entities){return null;} public void deleteAllInBatch(Iterable<Wallet> entities){} public void deleteAllByIdInBatch(Iterable<Long> ids){} public void deleteAllInBatch(){} public Wallet getOne(Long id){return null;} public Wallet getById(Long id){return null;} public Wallet getReferenceById(Long id){return null;} public <S extends Wallet> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example){return null;} public <S extends Wallet> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort){return null;} public <S extends Wallet> java.util.List<S> saveAll(Iterable<S> entities){return null;} public java.util.List<Wallet> findAll(){return null;} public java.util.List<Wallet> findAllById(Iterable<Long> ids){return null;} public <S extends Wallet> S save(S entity){return null;} public java.util.Optional<Wallet> findById(Long id){return null;} public boolean existsById(Long id){return false;} public long count(){return 0;} public void deleteById(Long id){} public void delete(Wallet entity){} public void deleteAllById(Iterable<? extends Long> ids){} public void deleteAll(Iterable<? extends Wallet> entities){} public void deleteAll(){} public java.util.List<Wallet> findAll(org.springframework.data.domain.Sort sort){return null;} public org.springframework.data.domain.Page<Wallet> findAll(org.springframework.data.domain.Pageable pageable){return null;} public <S extends Wallet> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example){return null;} public <S extends Wallet> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable){return null;} public <S extends Wallet> long count(org.springframework.data.domain.Example<S> example){return 0;} public <S extends Wallet> boolean exists(org.springframework.data.domain.Example<S> example){return false;} public <S extends Wallet, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction){return null;}
}

abstract class TradeOrderRepositoryStub implements TradeOrderRepository {
    public void flush(){} public <S extends TradeOrder> S saveAndFlush(S entity){return null;} public <S extends TradeOrder> java.util.List<S> saveAllAndFlush(Iterable<S> entities){return null;} public void deleteAllInBatch(Iterable<TradeOrder> entities){} public void deleteAllByIdInBatch(Iterable<Long> ids){} public void deleteAllInBatch(){} public TradeOrder getOne(Long id){return null;} public TradeOrder getById(Long id){return null;} public TradeOrder getReferenceById(Long id){return null;} public <S extends TradeOrder> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example){return null;} public <S extends TradeOrder> java.util.List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort){return null;} public <S extends TradeOrder> java.util.List<S> saveAll(Iterable<S> entities){return null;} public java.util.List<TradeOrder> findAll(){return null;} public java.util.List<TradeOrder> findAllById(Iterable<Long> ids){return null;} public <S extends TradeOrder> S save(S entity){return null;} public java.util.Optional<TradeOrder> findById(Long id){return null;} public boolean existsById(Long id){return false;} public long count(){return 0;} public void deleteById(Long id){} public void delete(TradeOrder entity){} public void deleteAllById(Iterable<? extends Long> ids){} public void deleteAll(Iterable<? extends TradeOrder> entities){} public void deleteAll(){} public java.util.List<TradeOrder> findAll(org.springframework.data.domain.Sort sort){return null;} public org.springframework.data.domain.Page<TradeOrder> findAll(org.springframework.data.domain.Pageable pageable){return null;} public <S extends TradeOrder> java.util.Optional<S> findOne(org.springframework.data.domain.Example<S> example){return null;} public <S extends TradeOrder> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable){return null;} public <S extends TradeOrder> long count(org.springframework.data.domain.Example<S> example){return 0;} public <S extends TradeOrder> boolean exists(org.springframework.data.domain.Example<S> example){return false;} public <S extends TradeOrder, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction){return null;}
}
