package com.example.shardedSagaWallet.services.steps;

import com.example.shardedSagaWallet.entities.Wallet;
import com.example.shardedSagaWallet.repositories.WalletRepository;
import com.example.shardedSagaWallet.services.saga.SagaContext;
import com.example.shardedSagaWallet.services.saga.SagaStep;
import com.example.shardedSagaWallet.services.saga.SagaStepFactory;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@Slf4j
public class CreditDestinationWalletStep implements SagaStep {
    private final WalletRepository walletRepository;


    @Override
    @Transactional
    public boolean execute(SagaContext context) {
        // step 1 : Get the destination wallet id from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Crediting destination wallet {} with amount {}", toWalletId, amount);
        // Step 2 : Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        // Step 3 : Credit the destination wallet
        log.info("Wallet fetched with balance {}", wallet.getBalance());
        context.put("original_To_WalletBalance", wallet.getBalance());
        wallet.credit(amount);
        walletRepository.save(wallet);
        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("to_Wallet_Balance_AfterCredit", wallet.getBalance());
        // Step 4 : Update the context with the changes
        log.info("Credit destination wallet step executed successfully");
        return true;
    }

    @Override
    public boolean compensate(SagaContext context) {
        // step 1 : Get the destination wallet id from the context
        Long toWalletId = context.getLong("toWalletId");
        BigDecimal amount = context.getBigDecimal("amount");

        log.info("Compensating credit of destination wallet {} with amount {}", toWalletId, amount);
        // Step 2 : Fetch the destination wallet from the database with a lock
        Wallet wallet = walletRepository.findByIdWithLock(toWalletId)
                .orElseThrow(()-> new RuntimeException("Wallet not found"));
        // Step 3 : Credit the destination wallet
        log.info("Wallet fetched with balance {}", wallet.getBalance());

        context.put("original_To_WalletBalance", wallet.getBalance());
        wallet.debit(amount);
        walletRepository.save(wallet);

        log.info("Wallet saved with balance {}", wallet.getBalance());
        context.put("to_Wallet_Balance_AfterCreditCompensation", wallet.getBalance());
        // Step 4 : Update the context with the changes
        log.info("Credit compensation of destination wallet step executed successfully");
        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString();
    }
}
