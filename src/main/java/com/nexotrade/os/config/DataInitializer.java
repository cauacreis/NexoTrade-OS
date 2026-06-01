package com.nexotrade.os.config;

import com.nexotrade.os.model.Wallet;
import com.nexotrade.os.repository.WalletRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final WalletRepository walletRepository;

    public DataInitializer(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (walletRepository.count() == 0) {
            Wallet initialWallet = new Wallet(new BigDecimal("10000.00"), BigDecimal.ZERO);
            walletRepository.save(initialWallet);
            System.out.println("[NEXOTRADE INFO] Carteira virtual inicializada com $ 10.000,00 USD.");
        }
    }
}
