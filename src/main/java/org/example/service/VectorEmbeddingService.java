package org.example.service;

import org.example.constant.MilvusConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 向量嵌入服务。
 * 通过 Spring AI EmbeddingModel 调用 OpenAI text-embedding-3-small，
 * 文档向量和查询向量始终使用同一个自动配置模型。
 */
@Service
public class VectorEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddingService.class);

    @Autowired
    private EmbeddingModel embeddingModel;

    @Value("${spring.ai.openai.embedding.options.model:text-embedding-3-small}")
    private String modelName;

    @jakarta.annotation.PostConstruct
    public void init() {
        if (embeddingModel == null) {
            throw new IllegalStateException("OpenAI EmbeddingModel is not configured; check OPENAI_API_KEY");
        }
        // 不在启动阶段调用 embeddingModel.dimensions()：部分 Spring AI 实现会通过网络请求读取维度，
        // 这会让应用启动依赖 OpenAI 网络可用性。首次文档索引时会校验实际返回维度。
        logger.info("OpenAI Embedding 初始化完成，模型: {}, 配置维度: {}, collection: {}",
                modelName, MilvusConstants.VECTOR_DIM, MilvusConstants.MILVUS_COLLECTION_NAME);
    }

    /** 生成单条文本向量。 */
    public List<Float> generateEmbedding(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }

        try {
            float[] vector = embeddingModel.embed(content);
            List<Float> result = toFloatList(vector);
            validateDimension(result.size());
            logger.debug("成功生成向量，内容长度: {}, 维度: {}", content.length(), result.size());
            return result;
        } catch (RuntimeException e) {
            if (e instanceof IllegalStateException) {
                throw e;
            }
            logger.error("生成 OpenAI 向量失败，内容长度: {}", content.length(), e);
            throw new RuntimeException("生成 OpenAI 向量失败: " + e.getMessage(), e);
        }
    }

    /** 批量生成文本向量，保持输入与输出顺序一致。 */
    public List<List<Float>> generateEmbeddings(List<String> contents) {
        if (contents == null || contents.isEmpty()) {
            return Collections.emptyList();
        }
        if (contents.stream().anyMatch(content -> content == null || content.isBlank())) {
            throw new IllegalArgumentException("批量向量化内容不能包含空文本");
        }

        try {
            List<float[]> vectors = embeddingModel.embed(contents);
            if (vectors == null || vectors.size() != contents.size()) {
                throw new IllegalStateException("OpenAI Embedding 返回数量与输入数量不一致");
            }

            List<List<Float>> result = new ArrayList<>(vectors.size());
            for (float[] vector : vectors) {
                List<Float> converted = toFloatList(vector);
                validateDimension(converted.size());
                result.add(converted);
            }
            logger.info("成功批量生成 OpenAI 向量，数量: {}, 维度: {}", result.size(), result.get(0).size());
            return result;
        } catch (RuntimeException e) {
            if (e instanceof IllegalStateException) {
                throw e;
            }
            logger.error("批量生成 OpenAI 向量失败，数量: {}", contents.size(), e);
            throw new RuntimeException("批量生成 OpenAI 向量失败: " + e.getMessage(), e);
        }
    }

    /** 查询向量与文档向量使用完全相同的 EmbeddingModel。 */
    public List<Float> generateQueryVector(String query) {
        return generateEmbedding(query);
    }

    private static List<Float> toFloatList(float[] vector) {
        if (vector == null || vector.length == 0) {
            throw new IllegalStateException("OpenAI Embedding 返回空向量");
        }
        List<Float> result = new ArrayList<>(vector.length);
        for (float value : vector) {
            result.add(value);
        }
        return result;
    }

    private static void validateDimension(int actualDimension) {
        if (actualDimension != MilvusConstants.VECTOR_DIM) {
            throw new IllegalStateException(String.format(
                    "OpenAI Embedding 维度不匹配: 实际 %d，Milvus 配置 %d。请确认 text-embedding-3-small 配置和 collection schema 一致",
                    actualDimension, MilvusConstants.VECTOR_DIM));
        }
    }

    /** 计算两个向量的余弦相似度。 */
    public float calculateCosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1 == null || vector2 == null || vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("向量维度不匹配");
        }

        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += vector1.get(i) * vector1.get(i);
            norm2 += vector2.get(i) * vector2.get(i);
        }
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
