package com.mykr.acgnhistoryanalyzer.service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TagDataInitializer implements CommandLineRunner {

    private final TagService tagService;

    public TagDataInitializer(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public void run(String... args) {
        tagService.ensureDefaultTags();
    }
}
