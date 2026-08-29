package com.example.shardedSagaWallet.services.steps;

import com.example.shardedSagaWallet.entities.Transaction;
import com.example.shardedSagaWallet.entities.TransactionStatus;
import com.example.shardedSagaWallet.repositories.TransactionRepository;
import com.example.shardedSagaWallet.services.saga.SagaContext;
import com.example.shardedSagaWallet.services.saga.SagaStep;
import com.example.shardedSagaWallet.services.saga.SagaStepFactory;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@AllArgsConstructor
public class UpdateTransactionStatus implements SagaStep {

    private final TransactionRepository transactionRepository;

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");

        log.info("Updating transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));

        context.put("OriginalTransactionStatus", transaction.getStatus());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        log.info("Transaction status updated for transaction {}", transactionId);

        context.put("Transaction_Status_After_Update", transaction.getStatus());

        log.info("Update transaction status step executed successfully");

        return false;
    }

    @Override
    public boolean compensate(SagaContext context) {
        Long transactionId = context.getLong("transactionId");

        log.info("Updating Compensating Transaction status for transaction {}", transactionId);

        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found"));

        TransactionStatus orignalTransactionStatus = (TransactionStatus) context.get("OriginalTransactionStatus");

        log.info("Transaction fetched with original transactionStatus as : {}", orignalTransactionStatus);

        transaction.setStatus(orignalTransactionStatus);
        transactionRepository.save(transaction);

        log.info("Transaction status reverted back to original and saved!");

        context.put("Transaction_Status_After_Compensate", transaction.getStatus());

        log.info("Compensated transaction step executed successfully!");

        return true;
    }

    @Override
    public String getStepName() {
        return SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString();
    }
}
