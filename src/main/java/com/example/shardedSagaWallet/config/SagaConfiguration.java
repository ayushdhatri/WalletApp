package com.example.shardedSagaWallet.config;


import com.example.shardedSagaWallet.services.saga.SagaStepInterface;
import com.example.shardedSagaWallet.services.saga.SagaStepFactory;
import com.example.shardedSagaWallet.services.steps.CreditDestinationWalletStepInterface;
import com.example.shardedSagaWallet.services.steps.DebitSourceWalletStepInterface;
import com.example.shardedSagaWallet.services.steps.UpdateTransactionStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SagaConfiguration {
    @Bean
    public Map<String, SagaStepInterface> sagaStepMap(
        DebitSourceWalletStepInterface debitSourceWalletStep,
        CreditDestinationWalletStepInterface creditDestinationWalletStep,
        UpdateTransactionStatus updateTransactionStatus
    )
    {
        Map<String, SagaStepInterface> sagaStepMap = new HashMap<>();
        sagaStepMap.put(SagaStepFactory.SagaStepType.CREDIT_DESTINATION_WALLET_STEP.toString(), creditDestinationWalletStep);
        sagaStepMap.put(SagaStepFactory.SagaStepType.DEBIT_SOURCE_WALLET_STEP.toString(), debitSourceWalletStep);
        sagaStepMap.put(SagaStepFactory.SagaStepType.UPDATE_TRANSACTION_STATUS_STEP.toString(), updateTransactionStatus);
        return sagaStepMap;

    }
}
