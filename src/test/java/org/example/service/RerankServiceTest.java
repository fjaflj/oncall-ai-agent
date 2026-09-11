package org.example.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RerankServiceTest {

    @Test
    void ranksCandidatesByCrossEncoderScoreAndKeepsFinalTopK() {
        VectorSearchService.SearchResult first = result("first");
        VectorSearchService.SearchResult second = result("second");
        VectorSearchService.SearchResult third = result("third");

        RerankService.RerankResult firstScore = rerankResult(0, 0.2f);
        RerankService.RerankResult secondScore = rerankResult(1, 0.95f);
        RerankService.RerankResult thirdScore = rerankResult(2, 0.6f);

        List<VectorSearchService.SearchResult> ranked = RerankService.rankCandidates(
                List.of(first, second, third),
                List.of(firstScore, secondScore, thirdScore),
                2);

        assertEquals(List.of("second", "third"), ranked.stream()
                .map(VectorSearchService.SearchResult::getContent)
                .toList());
    }

    private static VectorSearchService.SearchResult result(String content) {
        VectorSearchService.SearchResult result = new VectorSearchService.SearchResult();
        result.setContent(content);
        return result;
    }

    private static RerankService.RerankResult rerankResult(int index, float score) {
        RerankService.RerankResult result = new RerankService.RerankResult();
        result.setIndex(index);
        result.setRelevanceScore(score);
        return result;
    }
}
