package com.mykr.acgnhistoryanalyzer.service;

@FunctionalInterface
public interface SubjectImportProgressListener {

    void onItemProcessing(int currentIndex, int totalCount, String displayTitle);
}
