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

import java.util.List;

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

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.PENDING)
                .orElse(SagaStep.builder().sagaInstanceId(sagaInstanceId).stepName(stepName).status(StepStatus.PENDING).build());

        if(sagaStepDB.getId() == null){
            sagaStepDB = sagaStepRepository.save(sagaStepDB);
        }

        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsRunning();
            sagaStepRepository.save(sagaStepDB);
            boolean success = step.execute(sagaContext);

            if(success){
                sagaStepDB.markAsCompleted();
                sagaStepRepository.save(sagaStepDB);

                sagaInstance.setCurrentStep(stepName);
                sagaInstance.setStatus(SagaStatus.RUNNING);
                sagaInstanceRepository.save(sagaInstance);

                log.info("Step {} executed successfully", stepName);
                return true;
            }
            else{
                sagaStepDB.markAsFailed();
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
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("saga with instance id:" + sagaInstanceId+" not found!"));

        SagaStepInterface step = sagaStepFactory.getSagaStep(stepName);
        if(step == null){
            log.error("Saga step not found for step name : {}", stepName);
            throw new RuntimeException("Saga Step not found");
        }

        SagaStep sagaStepDB = sagaStepRepository.findBySagaInstanceIdAndStepNameAndStatus(sagaInstanceId, stepName, StepStatus.COMPLETED)
                .orElseThrow(()-> new RuntimeException("No Valid SagaStep found !"));

        if(sagaStepDB.getId() == null){
            log.info("Step {} not found in the db for saga instance {}, so it is already compensated or not executed",stepName, sagaInstanceId);
        }


        try{
            SagaContext sagaContext = objectMapper.readValue(sagaInstance.getContext(), SagaContext.class);
            sagaStepDB.markAsCompensating();
            sagaStepRepository.save(sagaStepDB);
            boolean success = step.compensate(sagaContext);

            if(success){
                sagaStepDB.markAsCompensated();
                sagaStepRepository.save(sagaStepDB);
                log.info("Step {} compensated successfully", stepName);
                return true;
            }
            else{
                sagaStepDB.markAsFailed();
                sagaStepRepository.save(sagaStepDB);
                log.info("Step {} failed", stepName);
                return false;
            }
        }
        catch (Exception ex){
            sagaStepDB.markAsFailed();
            sagaStepRepository.save(sagaStepDB);
            log.error(ex.getMessage());
            return false;
        }


    }

    @Override
    public SagaInstance getSagaInstance(Long sagaInstanceId) {
        return sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(() -> new RuntimeException("Saga Instance with id :"+ sagaInstanceId + " not found"));
    }

    @Override
    public void compensateSaga(Long sagaInstanceId) {
        SagaInstance sagaInstance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(()-> new RuntimeException("Saga Instance not found"));
        // mark the saga status as compensating in db
        sagaInstance.setStatus(SagaStatus.COMPENSATING);
        sagaInstanceRepository.save(sagaInstance);

        List<SagaStep> completedSteps = sagaStepRepository.findCompletedStepsBySagaInstanceId(sagaInstanceId);
        boolean allCompensated = true;
        for(SagaStep completedStep : completedSteps){
            boolean compensated = this.compensateStep(completedStep.getSagaInstanceId(), completedStep.getStepName());
            if(compensated)
                log.info("Step name : " + completedStep.getStepName() + " compensated successfully");
            else {
                allCompensated = false;
                log.error("Step name : " + completedStep.getStepName() + " compensated failed!");
            }
        }

        if(allCompensated){
            sagaInstance.setStatus(SagaStatus.COMPENSATED);
            sagaInstanceRepository.save(sagaInstance);
            log.info("All steps compensated successfully {} ", sagaInstanceId);
        }
        else{
            log.info("Few steps failed for saga");
        }

    }

    @Override
    public void failSaga(Long sagaInstanceId) {
        SagaInstance instance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(()-> new RuntimeException("Saga Instance not found"));
        instance.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(instance);
    }

    @Override
    public void completeSaga(Long sagaInstanceId) {
        SagaInstance instance = sagaInstanceRepository.findById(sagaInstanceId).orElseThrow(()-> new RuntimeException("Saga Instance not found"));
        instance.setStatus(SagaStatus.COMPLETED);
        sagaInstanceRepository.save(instance);
    }


}
