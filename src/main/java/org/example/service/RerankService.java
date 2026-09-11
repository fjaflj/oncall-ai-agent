package org.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 二阶段 RAG 重排序服务，支持通过 HTTP 接入 Cross Encoder/Reranker。 */
@Service
public class RerankService {

    private static final Logger logger = LoggerFactory.getLogger(RerankService.class);

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${rag.rerank.enabled:false}")
    private boolean enabled;

    @Value("${rag.rerank.url:https://api.siliconflow.com/v1/rerank}")
    private String rerankUrl;

    @Value("${rag.rerank.api-key:}")
    private String apiKey;

    @Value("${rag.rerank.model:BAAI/bge-reranker-v2-m3}")
    private String model;

    @Value("${rag.rerank.recall-top-k:12}")
    private int recallTopK;

    @Value("${rag.rerank.fail-on-error:false}")
    private boolean failOnError;

    public RerankService(RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {
        this.restClientBuilder = restClientBuilder;
        this.objectMapper = objectMapper;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getRecallTopK(int finalTopK) {
        return enabled ? Math.max(finalTopK, recallTopK) : finalTopK;
    }

    /** 对 Milvus 候选结果进行精排；未启用或失败时默认回退到向量召回。 */
    public List<VectorSearchService.SearchResult> rerank(
            String query,
            List<VectorSearchService.SearchResult> candidates,
            int finalTopK) {

        List<VectorSearchService.SearchResult> fallback = limit(candidates, finalTopK);
        if (!enabled || candidates == null || candidates.size() <= 1) {
            return fallback;
        }
        if (apiKey == null || apiKey.isBlank()) {
            return handleFailure("Rerank 已开启，但 rag.rerank.api-key 未配置", fallback, null);
        }

        try {
            List<String> documents = candidates.stream()
                    .map(VectorSearchService.SearchResult::getContent)
                    .toList();
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", model);
            requestBody.put("query", query);
            requestBody.put("documents", documents);
            requestBody.put("top_n", Math.min(finalTopK, documents.size()));
            requestBody.put("return_documents", false);

            String responseBody = restClientBuilder.build()
                    .post()
                    .uri(rerankUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            RerankResponse response = objectMapper.readValue(responseBody, RerankResponse.class);
            List<VectorSearchService.SearchResult> ranked = rankCandidates(candidates, response.getResults(), finalTopK);
            if (ranked.isEmpty()) {
                return handleFailure("Rerank 返回空结果", fallback, null);
            }
            logger.info("Rerank 完成: 候选数={}, 最终返回={}, 模型={}", candidates.size(), ranked.size(), model);
            return ranked;
        } catch (Exception e) {
            return handleFailure("Rerank 请求失败: " + e.getMessage(), fallback, e);
        }
    }

    static List<VectorSearchService.SearchResult> rankCandidates(
            List<VectorSearchService.SearchResult> candidates,
            List<RerankResult> rerankResults,
            int finalTopK) {
        if (candidates == null || rerankResults == null || rerankResults.isEmpty()) {
            return List.of();
        }
        List<VectorSearchService.SearchResult> ranked = new ArrayList<>();
        rerankResults.stream()
                .filter(result -> result.getIndex() != null
                        && result.getIndex() >= 0
                        && result.getIndex() < candidates.size())
                .sorted(Comparator.comparing(
                        result -> result.getRelevanceScore() == null
                                ? Float.NEGATIVE_INFINITY : result.getRelevanceScore(),
                        Comparator.reverseOrder()))
                .limit(Math.max(finalTopK, 0))
                .forEach(result -> ranked.add(candidates.get(result.getIndex())));
        return ranked;
    }

    private List<VectorSearchService.SearchResult> limit(
            List<VectorSearchService.SearchResult> candidates, int finalTopK) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(candidates.subList(0, Math.min(Math.max(finalTopK, 0), candidates.size())));
    }

    private List<VectorSearchService.SearchResult> handleFailure(
            String message,
            List<VectorSearchService.SearchResult> fallback,
            Exception exception) {
        if (failOnError) {
            throw exception == null
                    ? new IllegalStateException(message)
                    : new IllegalStateException(message, exception);
        }
        logger.warn("{}，回退到 Milvus 向量召回结果", message, exception);
        return fallback;
    }

    static class RerankResponse {
        private List<RerankResult> results = List.of();

        public List<RerankResult> getResults() { return results; }
        public void setResults(List<RerankResult> results) { this.results = results; }
    }

    static class RerankResult {
        private Integer index;

        @JsonProperty("relevance_score")
        private Float relevanceScore;

        public Integer getIndex() { return index; }
        public void setIndex(Integer index) { this.index = index; }
        public Float getRelevanceScore() { return relevanceScore; }
        public void setRelevanceScore(Float relevanceScore) { this.relevanceScore = relevanceScore; }
    }
}
