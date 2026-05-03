package com.mykr.acgnhistoryanalyzer.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mykr.acgnhistoryanalyzer.request.BangumiSubjectBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectCollectionBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.request.UserSubjectRecordBatchImportRequest;
import com.mykr.acgnhistoryanalyzer.response.LlmAnalysisResponse;
import com.mykr.acgnhistoryanalyzer.response.LlmQuestionAnswerResponse;
import com.mykr.acgnhistoryanalyzer.service.BangumiSeasonImportService;
import com.mykr.acgnhistoryanalyzer.service.LlmAnalysisService;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectCollectionService;
import com.mykr.acgnhistoryanalyzer.service.UserSubjectRecordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BackgroundJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(BackgroundJobProcessor.class);

    private final Object lock = new Object();

    private final BackgroundJobRepository backgroundJobRepository;
    private final ObjectMapper objectMapper;
    private final BangumiSeasonImportService bangumiSeasonImportService;
    private final UserSubjectCollectionService userSubjectCollectionService;
    private final UserSubjectRecordService userSubjectRecordService;
    private final LlmAnalysisService llmAnalysisService;

    public BackgroundJobProcessor(
            BackgroundJobRepository backgroundJobRepository,
            ObjectMapper objectMapper,
            BangumiSeasonImportService bangumiSeasonImportService,
            UserSubjectCollectionService userSubjectCollectionService,
            UserSubjectRecordService userSubjectRecordService,
            LlmAnalysisService llmAnalysisService
    ) {
        this.backgroundJobRepository = backgroundJobRepository;
        this.objectMapper = objectMapper;
        this.bangumiSeasonImportService = bangumiSeasonImportService;
        this.userSubjectCollectionService = userSubjectCollectionService;
        this.userSubjectRecordService = userSubjectRecordService;
        this.llmAnalysisService = llmAnalysisService;
    }

    public void tryProcessNext() {
        synchronized (lock) {
            BackgroundJob job = backgroundJobRepository
                    .findFirstByStatusOrderByCreatedAtAsc(BackgroundJobStatus.PENDING)
                    .orElse(null);
            if (job == null) {
                return;
            }

            long startedAt = System.currentTimeMillis();
            job.setStatus(BackgroundJobStatus.RUNNING);
            job.setMessage("运行中");
            job.setProgress(5);
            backgroundJobRepository.save(job);
            bumpProgress(job.getId(), 8, "已开始处理");

            try {
                String resultJson = executeJob(job);
                job.setStatus(BackgroundJobStatus.SUCCEEDED);
                job.setResultJson(resultJson);
                job.setProgress(100);
                job.setMessage("完成");
                job.setErrorMessage(null);
            } catch (Exception ex) {
                job.setStatus(BackgroundJobStatus.FAILED);
                job.setProgress(100);
                String message = ex.getMessage();
                if (message == null || message.isBlank()) {
                    message = ex.getClass().getSimpleName();
                }
                job.setErrorMessage(message);
                job.setMessage("失败");
                log.warn("jobFailed id={} type={}", job.getId(), job.getJobType(), ex);
            }
            backgroundJobRepository.save(job);
            log.info(
                    "jobFinished id={} type={} status={} durationMs={}",
                    job.getId(),
                    job.getJobType(),
                    job.getStatus(),
                    System.currentTimeMillis() - startedAt
            );
        }
    }

    private void bumpProgress(String jobId, int progress, String message) {
        backgroundJobRepository.findById(jobId).ifPresent(entity -> {
            if (entity.getStatus() != BackgroundJobStatus.RUNNING) {
                return;
            }
            entity.setProgress(progress);
            entity.setMessage(message);
            backgroundJobRepository.save(entity);
        });
    }

    private String executeJob(BackgroundJob job) throws Exception {
        bumpProgress(job.getId(), 12, "执行中");
        JsonNode payload = objectMapper.readTree(job.getPayloadJson());
        bumpProgress(job.getId(), 18, "解析参数");
        return switch (job.getJobType()) {
            case BANGUMI_SUBJECT_PREVIEW -> {
                BangumiSubjectBatchImportRequest request =
                        objectMapper.treeToValue(payload, BangumiSubjectBatchImportRequest.class);
                bumpProgress(job.getId(), 35, "Bangumi 作品预览");
                yield objectMapper.writeValueAsString(Map.of(
                        "subjectImportPreview",
                        bangumiSeasonImportService.previewSubjectsBySource(request)
                ));
            }
            case BANGUMI_SUBJECT_IMPORT -> {
                BangumiSubjectBatchImportRequest request =
                        objectMapper.treeToValue(payload, BangumiSubjectBatchImportRequest.class);
                bumpProgress(job.getId(), 35, "Bangumi 作品写入");
                yield objectMapper.writeValueAsString(Map.of(
                        "subjectImport",
                        bangumiSeasonImportService.importSubjectsBySource(request)
                ));
            }
            case COLLECTION_IMPORT_PREVIEW -> {
                UserSubjectCollectionBatchImportRequest request =
                        objectMapper.treeToValue(payload, UserSubjectCollectionBatchImportRequest.class);
                bumpProgress(job.getId(), 30, "收藏预览");
                yield objectMapper.writeValueAsString(Map.of(
                        "collectionImportPreview",
                        userSubjectCollectionService.previewImportCollections(request)
                ));
            }
            case COLLECTION_IMPORT -> {
                UserSubjectCollectionBatchImportRequest request =
                        objectMapper.treeToValue(payload, UserSubjectCollectionBatchImportRequest.class);
                bumpProgress(job.getId(), 30, "收藏写入");
                yield objectMapper.writeValueAsString(Map.of(
                        "collectionImport",
                        userSubjectCollectionService.importCollections(request)
                ));
            }
            case RECORD_IMPORT_PREVIEW -> {
                UserSubjectRecordBatchImportRequest request =
                        objectMapper.treeToValue(payload, UserSubjectRecordBatchImportRequest.class);
                bumpProgress(job.getId(), 30, "记录预览");
                yield objectMapper.writeValueAsString(Map.of(
                        "recordImportPreview",
                        userSubjectRecordService.previewImportRecords(request)
                ));
            }
            case RECORD_IMPORT -> {
                UserSubjectRecordBatchImportRequest request =
                        objectMapper.treeToValue(payload, UserSubjectRecordBatchImportRequest.class);
                bumpProgress(job.getId(), 30, "记录写入");
                yield objectMapper.writeValueAsString(Map.of(
                        "recordImport",
                        userSubjectRecordService.importRecords(request)
                ));
            }
            case LLM_ANALYSIS -> {
                bumpProgress(job.getId(), 22, "大模型任务");
                Map<String, Object> body = runLlmJob(job.getId(), payload);
                bumpProgress(job.getId(), 92, "整理结果");
                yield objectMapper.writeValueAsString(body);
            }
        };
    }


    private Map<String, Object> runLlmJob(String jobId, JsonNode payload) {
        bumpProgress(jobId, 28, "调用大模型");
        String kind = payload.path("llmKind").asText("");
        Map<String, Object> body = new LinkedHashMap<>();
        switch (kind) {
            case "YEARLY" -> {
                Integer year = payload.hasNonNull("year") ? payload.get("year").asInt() : null;
                String category = textOrNull(payload, "category");
                LlmAnalysisResponse response = llmAnalysisService.generateYearlySummary(year, category);
                body.put("llmAnalysis", response);
            }
            case "QUARTERLY" -> {
                Integer year = payload.hasNonNull("year") ? payload.get("year").asInt() : null;
                Integer quarter = payload.hasNonNull("quarter") ? payload.get("quarter").asInt() : null;
                String category = textOrNull(payload, "category");
                LlmAnalysisResponse response = llmAnalysisService.generateQuarterlySummary(year, quarter, category);
                body.put("llmAnalysis", response);
            }
            case "TYPE_PREFERENCE" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateTypePreference(category));
            }
            case "SCORE_STRUCTURE" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateScoreStructure(category));
            }
            case "TAG_PREFERENCE" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateTagPreference(category));
            }
            case "RECOMMENDATION" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateRecommendations(category));
            }
            case "COLLECTION_BACKLOG" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateCollectionBacklog(category));
            }
            case "TASTE_PROFILE" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateTasteProfile(category));
            }
            case "PREFERENCE_CONTRADICTION" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generatePreferenceContradictions(category));
            }
            case "INTEREST_TREND" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateInterestTrend(category));
            }
            case "FRANCHISE_COMPLETION" -> {
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateFranchiseCompletion(category));
            }
            case "COMPREHENSIVE_REPORT" -> {
                Integer year = payload.hasNonNull("year") ? payload.get("year").asInt() : null;
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateComprehensiveReport(year, category));
            }
            case "HOME_INSIGHT" -> {
                Integer year = payload.hasNonNull("year") ? payload.get("year").asInt() : null;
                Integer quarter = payload.hasNonNull("quarter") ? payload.get("quarter").asInt() : null;
                String category = textOrNull(payload, "category");
                body.put("llmAnalysis", llmAnalysisService.generateHomeInsight(year, quarter, category));
            }
            case "QUESTION_ANSWER" -> {
                String question = payload.path("question").asText("");
                LlmQuestionAnswerResponse response = llmAnalysisService.answerQuestion(question);
                body.put("llmQuestionAnswer", response);
            }
            default -> throw new IllegalArgumentException("未知 llmKind：" + kind);
        }
        bumpProgress(jobId, 88, "大模型完成");
        return body;
    }

    private static String textOrNull(JsonNode payload, String field) {
        if (!payload.hasNonNull(field)) {
            return null;
        }
        String value = payload.get(field).asText("");
        return value.isBlank() ? null : value;
    }
}
