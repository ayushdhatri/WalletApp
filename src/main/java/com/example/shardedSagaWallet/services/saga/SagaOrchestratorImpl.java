package com.example.shardedSagaWallet.services.saga;

import com.example.shardedSagaWallet.entities.SagaInstance;
import com.example.shardedSagaWallet.entities.SagaStatus;

import com.example.shardedSagaWallet.entities.SagaStep;
import com.example.shardedSagaWallet.entities.StepStatus;
import com.example.shardedSagaWallet.repositories.SagaInstanceRepository;
import com.example.shardedSagaWallet.repositories.SagaStepRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
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

    private final SagaStepRepository sagaStepRepository;

    @Override
    @Transactional
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
    @Transactional
    public boolean executeStep(Long sagaInstanceId, String stepName) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("saga with instance id:" + sagaInstanceId+" not found!"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null){
            log.error("Saga step not found for step name : {}", stepName);
            throw new RuntimeException("Saga Step not found");
        }

//        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStatus(sagaInstanceId, StepStatus.PENDING)
//                .stream()
//                .filter((s)-> s.getStepName().equals(stepName))
//                .findFirst()
//                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());
        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null){
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.setStatus(StepStatus.RUNNING);
            sagaStepRepository.save(sagaStepDB);
            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully", stepName);
                return true;
            }
            else{
                sagaStepDB.setStatus(StepStatus.FAILED);
                sagaStepRepository.save(sagaStepDB);
                log.info("Step {} failed", stepName);
                return false;
            }
        }
        catch (Exception ex){
            sagaStepDB.setStatus(StepStatus.FAILED);
            sagaStepRepository.save(sagaStepDB);
            log.error(ex.getMessage());
            return false;
        }

    }

    @Override
    public boolean compensateStep(Long sagaInstanceId, String stepName) {
        // Fetch the saga instance from db using the saga instance Id

        // 2. Fetch the saga step from db using the saga instance id and step name

        // 3. Take the context from the saga instance and call the

        // 4. Update the appropriate status in the saga step


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
