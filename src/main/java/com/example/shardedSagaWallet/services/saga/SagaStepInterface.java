package com.example.shardedSagaWallet.services.saga;

public interface SagaStepInterface {
    boolean execute(SagaContext context);

    boolean compensate(SagaContext context);

    String getStepName();

}
