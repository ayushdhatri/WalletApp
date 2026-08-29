package com.example.shardedSagaWallet.services.steps;

import com.example.shardedSagaWallet.entities.Wallet;
import com.example.shardedSagaWallet.repositories.WalletRepository;
import com.example.shardedSagaWallet.services.saga.SagaContext;
import com.example.shardedSagaWallet.services.saga.SagaStepInterface;
import com.example.shardedSagaWallet.services.saga.SagaStepFactory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class DebitSourceWalletStepInterface implements SagaStepInterface {
    private WalletRepository walletRepository;

    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Debiting source wallet {} with amount {}", fromWalletId, amount);
        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId).orElseThrow(() -> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("original_Source_Wallet_Balance", wallet.getBalance());

        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("sourceWalletBalanceAfterDebit", wallet.getBalance());

        log.info("Debit done successfully");
        return true;

    }

    @Override
    @Transactional
    public boolean compensate(SagaContext context) {
        Long fromWalletId = context.getLong("fromWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating source wallet {} with amount {}", fromWalletId, amount);

        Wallet wallet = walletRepository.findByIdWithLock(fromWalletId).orElseThrow(()-> new RuntimeException("Wallet not found"));

        log.info("Wallet fetched with balance {}", wallet.getBalance());

        wallet.credit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("Source_Wallet_Balance_AfterCredit_Compensation", wallet.getBalance());

        log.info("Compensating source wallet setup executed successfully");

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString();
    }
}
