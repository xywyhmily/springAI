package com.zjx.star.springai;

import com.zjx.star.springai.utils.VectorDistanceUtils;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
class SpringAiApplicationTests {

    @Autowired
    private OpenAiEmbeddingModel openAiEmbeddingModel;

    @Autowired
    private VectorStore simpleVectorStore;

    @Test
    void testVectorStore() {

        Resource resource = new FileSystemResource("中二知识笔记.pdf");

//        创建pdf读取器
        PagePdfDocumentReader reader = new PagePdfDocumentReader(
                resource, // 文件源
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.defaults())
                        .withPagesPerDocument(1) // 每1页PDF作为一个Document
                        .build()
        );

//        读取pdf文档，拆分为Document
        List<Document> read = reader.read();

//        写入向量库
        simpleVectorStore.add(read);

//        搜索
        List<Document> documents = simpleVectorStore.similaritySearch("长善救失");
        if (documents==null){
            System.out.println("没有找到相关文档");
            return;
        }
        for (Document document : documents) {
            System.out.println(document.getFormattedContent());
        }

    }

    @Test
    void contextLoads() {
        float[] embed = openAiEmbeddingModel.embed("你好，我是抖音小美");
        System.out.println(Arrays.toString(embed));
    }

    @Test
    void testEmbeddingModel() {
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
        // 2.向量化
        // 2.1.先将查询文本向量化
        float[] queryVector = openAiEmbeddingModel.embed(query);

        // 2.2.再将比较文本向量化，放到一个数组
        List<float[]> textVectors = openAiEmbeddingModel.embed(Arrays.asList(texts));

        // 3.比较欧氏距离
        // 3.1.把查询文本自己与自己比较，肯定是相似度最高的
        System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, queryVector));
        // 3.2.把查询文本与其它文本比较
        for (float[] textVector : textVectors) {
            System.out.println(VectorDistanceUtils.euclideanDistance(queryVector, textVector));
        }
        System.out.println("------------------");

        // 4.比较余弦距离
        // 4.1.把查询文本自己与自己比较，肯定是相似度最高的
        System.out.println(VectorDistanceUtils.cosineDistance(queryVector, queryVector));
        // 4.2.把查询文本与其它文本比较
        for (float[] textVector : textVectors) {
            System.out.println(VectorDistanceUtils.cosineDistance(queryVector, textVector));
        }
    }

}
