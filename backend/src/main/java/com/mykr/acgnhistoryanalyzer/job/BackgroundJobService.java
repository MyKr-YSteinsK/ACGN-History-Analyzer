package com.mykr.acgnhistoryanalyzer.job;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BackgroundJobService {

    private final BackgroundJobRepository backgroundJobRepository;

    public BackgroundJobService(BackgroundJobRepository backgroundJobRepository) {
        this.backgroundJobRepository = backgroundJobRepository;
    }

    @Transactional
    public BackgroundJobStartResponse enqueue(BackgroundJobEnqueueRequest request) {
        String id = UUID.randomUUID().toString();
        BackgroundJob job = new BackgroundJob();
        job.setId(id);
        job.setJobType(request.getJobType());
        job.setStatus(BackgroundJobStatus.PENDING);
        job.setPayloadJson(request.getPayload().toString());
        job.setProgress(0);
        job.setMessage("排队中");
        backgroundJobRepository.save(job);
        return new BackgroundJobStartResponse(id);
    }

    public BackgroundJobResponse get(String id) {
        return backgroundJobRepository.findById(id)
                .map(BackgroundJobResponse::fromEntity)
                .orElse(null);
    }
}
