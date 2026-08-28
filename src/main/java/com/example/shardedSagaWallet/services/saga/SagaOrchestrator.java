package com.example.shardedSagaWallet.services.saga;

import com.example.shardedSagaWallet.entities.SagaInstance;

public interface SagaOrchestrator {

    Long startSaga(SagaContext context);// take context object and create a new saga and return Id

    boolean executeStep(Long sagaInstanceId, String stepName);

    boolean compensateStep(Long sagaInstanceId, String stepName);

    SagaInstance getSagaInstance(Long sagaInstanceId);

    void compensateSaga(Long sagaInstanceId);

    void failSaga(Long sagaInstanceId);

}
