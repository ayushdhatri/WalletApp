package com.example.shardedSagaWallet.services.steps;

import com.example.shardedSagaWallet.services.saga.SagaContext;
import com.example.shardedSagaWallet.services.saga.SagaStep;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UpdateTransactionStatus implements SagaStep {

    @Override
    public boolean execute(SagaContext context) {
        Long transactionId = context.getLong("transactionId");

        log.info("Updating transaction status for transaction {}", transactionId);

        return false;
    }

    @Override
    public boolean compensate(SagaContext context) {
        return false;
    }

    @Override
    public String getStepName() {
        return "";
    }
}
