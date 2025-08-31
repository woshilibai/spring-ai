package com.evermodel.service.agg.aidemo;

import com.evermodel.service.agg.aidemo.utils.VectorDistanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ResourceUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@SpringBootTest
class AiDemoApplicationTests {

    @Autowired
    private OpenAiEmbeddingModel embeddingModel;
    @Autowired
    private VectorStore vectorStore;

    @Test
    void testEmbedding() {

        // 1.测试数据
        // 1.1.用来查询的文本，国际冲突
        String query = "global conflicts";

        // 1.2.用来做比较的文本
        String[] texts = new String[]{
                "哈马斯称加沙下阶段停火谈判仍在进行 以方尚未做出承诺",
                "土耳其、芬兰、瑞典与北约代表将继续就瑞典“入约”问题进行谈判",
                "日本航空基地水井中检测出有机氟化物超标",
                "国家游泳中心（水立方）：恢复游泳、嬉水乐园等水上项目运营",
                "我国首次在空间站开展舱外辐射生物学暴露实验",
        };

        //  2.向量化测试数据
        // 2.1.先将查询文本向量化
        float[] queryVector = embeddingModel.embed(query);
        log.info("queryVector: {}", Arrays.toString(queryVector));

        // 2.2.再将比较文本向量化，放到一个数组
        List<float[]> textVectors = embeddingModel.embed(Arrays.asList(texts));

        //  比较欧式距离
        log.info("queryVector与自身的欧式距离：{}", VectorDistanceUtils.euclideanDistance(queryVector, queryVector));
        for (int i = 0; i < texts.length; i++) {
            log.info("queryVector与{}的欧式距离：{}", texts[i], VectorDistanceUtils.euclideanDistance(queryVector, textVectors.get(i)));
        }

        log.info("-".repeat(100));

        log.info("queryVector与自身的余弦距离：{}", VectorDistanceUtils.cosineDistance(queryVector, queryVector));
        for (int i = 0; i < texts.length; i++) {
            log.info("queryVector与{}的余弦距离：{}", texts[i], VectorDistanceUtils.cosineDistance(queryVector, textVectors.get(i)));
        }

    }

    @Test
    public void testVectorStore() {
        Resource resource = new FileSystemResource("/Users/tianwl/IdeaProjects/ai-demo/src/main/resources/pdf/中二知识笔记.pdf");
        // 1.创建PDF的读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource,   //  文件资源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1)    //  按页切割为 文档
                        .build());

        //  2.读取PDF文档，拆分为Document
        List<Document> documents = reader.read();

        //  3.写入向量数据库
        vectorStore.add(documents);

        //  4.查询向量数据库，获取相似度最高的文档块，后续用于和查询文本一起组装为提示词发送给LLM进行问答（RAG原理）
        String query = "论语中教育的目的是什么";
        log.info("query: {}", query);

        SearchRequest searchRequest = SearchRequest.builder()
                .query( query)
                .topK(1)
                .similarityThreshold(0.6f)
                .filterExpression("file_name == '中二知识笔记.pdf'")
                .build();

        log.info("相似度最高的文档块：");
        Objects.requireNonNull(vectorStore.similaritySearch(searchRequest)).forEach(document -> {
            log.info("document.id: {}", document.getId());
            log.info("document.score: {}", document.getScore());
            log.info("document.text: {}", document.getText());
        });
    }

}
