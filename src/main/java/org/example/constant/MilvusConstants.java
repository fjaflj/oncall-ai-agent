package org.example.constant;

public class MilvusConstants {
    
    /**
     * Milvus 数据库名称
     */
    public static final String MILVUS_DB_NAME = "default";
    
    /**
     * Milvus 集合名称
     */
    public static final String MILVUS_COLLECTION_NAME = "oncall_knowledge_openai";
    
    /**
     * text-embedding-3-small 的默认向量维度
     */
    public static final int VECTOR_DIM = 1536;
    
    /**
     * ID字段最大长度
     */
    public static final int ID_MAX_LENGTH = 256;
    
    /**
     * Content字段最大长度
     */
    public static final int CONTENT_MAX_LENGTH = 8192;
    
    /**
     * 默认分片数
     */
    public static final int DEFAULT_SHARD_NUMBER = 2;
    
    private MilvusConstants() {
        // 工具类，禁止实例化
    }
}
