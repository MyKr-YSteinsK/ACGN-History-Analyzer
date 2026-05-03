package com.mykr.acgnhistoryanalyzer.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackgroundJobWorker {

    private static final Logger log = LoggerFactory.getLogger(BackgroundJobWorker.class);

    private final BackgroundJobProcessor backgroundJobProcessor;

    public BackgroundJobWorker(BackgroundJobProcessor backgroundJobProcessor) {
        this.backgroundJobProcessor = backgroundJobProcessor;
    }

    @Scheduled(fixedDelayString = "${app.jobs.poll-interval-ms:750}")
    public void runPendingJobs() {
        long t0 = System.currentTimeMillis();
        try {
            backgroundJobProcessor.tryProcessNext();
        } catch (Exception ex) {
            log.error("jobWorkerTick failed", ex);
        } finally {
            long ms = System.currentTimeMillis() - t0;
            if (ms > 2000) {
                log.warn("jobWorkerTick slow durationMs={}", ms);
            }
        }
    }
}

