package com.mykr.acgnhistoryanalyzer.service;

@FunctionalInterface
public interface BangumiImportProgressReporter {

    void update(int percent, String stage, String message);
}
