package com.mykr.acgnhistoryanalyzer.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackgroundJobWorker {

    private final BackgroundJobProcessor backgroundJobProcessor;

    public BackgroundJobWorker(BackgroundJobProcessor backgroundJobProcessor) {
        this.backgroundJobProcessor = backgroundJobProcessor;
    }

    @Scheduled(fixedDelayString = "${app.jobs.poll-interval-ms:750}")
    public void runPendingJobs() {
        backgroundJobProcessor.tryProcessNext();
    }
}
