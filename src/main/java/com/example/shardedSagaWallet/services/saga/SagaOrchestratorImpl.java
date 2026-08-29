package com.example.shardedSagaWallet.services.saga;

import com.example.shardedSagaWallet.entities.SagaInstance;
import com.example.shardedSagaWallet.entities.SagaStatus;

import com.example.shardedSagaWallet.repositories.SagaInstanceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestratorImpl implements SagaOrchestrator {

    private final ObjectMapper objectMapper;

    private final SagaInstanceRepository sagaInstanceRepository;

    private final SagaStepFactory sagaStepFactory;

    @Override
    public Long startSaga(SagaContext context) {
        // we need to get a JSON String from context (which is a java object)
        try{
            String contextJson = objectMapper.writeValueAsString(context);// convert the context to a json as a string
            SagaInstance sagaInstance = SagaInstance.builder()
                    .context(contextJson)
                    .status(SagaStatus.STARTED)
                    .build();
            sagaInstanceRepository.save(sagaInstance);
            return sagaInstance.getId();
        }catch (Exception ignored){
            log.error(ignored.getMessage());
            return null;
        }
    }

    @Override
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("saga with instance id:" + sagaInstanceId+" not found!"));

        SagaStepInterface sagaStep = sagaStepFactory.getSagaStep(stepName);
        return false;
    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        return false;
    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return null;
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {

    }

    @Override
    public void failSaga(Long sagaInstanceId) {

    }
}
