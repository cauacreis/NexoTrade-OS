package com.nexotrade.os.controller;

import com.nexotrade.os.dto.TradeRequestDTO;
import com.nexotrade.os.model.OrderType;
import com.nexotrade.os.model.Wallet;
import com.nexotrade.os.repository.WalletRepository;
import com.nexotrade.os.service.TradingEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TradingController {

    private final TradingEngineService tradingEngineService;
    private final WalletRepository walletRepository;

    public TradingController(TradingEngineService tradingEngineService, WalletRepository walletRepository) {
        this.tradingEngineService = tradingEngineService;
        this.walletRepository = walletRepository;
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWalletBalance() {
        return walletRepository.findAll().stream().findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/trade/execute")
    public ResponseEntity<?> executeTrade(@RequestBody TradeRequestDTO request) {
        try {
            if (request.getAction() == OrderType.BUY) {
                if (request.getAmountUsd() == null) {
                    return ResponseEntity.badRequest().body("amountUsd é obrigatório para compra.");
                }
                return ResponseEntity.ok(tradingEngineService.executeBuy(request.getAmountUsd()));
            } else if (request.getAction() == OrderType.SELL) {
                if (request.getAmountBtc() == null) {
                    return ResponseEntity.badRequest().body("amountBtc é obrigatório para venda.");
                }
                return ResponseEntity.ok(tradingEngineService.executeSell(request.getAmountBtc()));
            } else {
                return ResponseEntity.badRequest().body("Ação inválida. Use BUY ou SELL.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro interno: " + e.getMessage());
        }
    }
}
