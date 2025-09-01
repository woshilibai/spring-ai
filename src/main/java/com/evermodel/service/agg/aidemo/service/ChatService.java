package com.evermodel.service.agg.aidemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.Media;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;

/**
 * @author tianwl
 * @date 2025/8/30
 * @description
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient qwChatClient;

    private final ChatClient chatMemoryClient;

    private final ChatClient multiModalClient;

    private final ChatClient ragChatClient;

    private final VectorStore vectorStore;

    private final ChatClient toolsChatClient;

    private final ChatClient toolsChatStreamClient;

    //  存储 chatId-> 文件资源
    private final Map<Integer, Resource> map = new ConcurrentHashMap();

    /**
     * 阻塞式会话
     */
    public String chat(String prompt) {
        return qwChatClient.prompt().user(prompt).call().content();
    }

    /**
     * 流式会话
     */
    public Flux<String> chatStream(String prompt) {
        return qwChatClient.prompt().user(prompt).stream().content();
    }

    /**
     * 会话记忆
     */
    public Flux<String> chatMemory(String prompt, Integer chatId) {
        return chatMemoryClient.prompt()
                .user(prompt)
                .advisors(a->a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }

    /**
     * 多模态混合输入
     * 多媒体文件为音频时，千问模型对于openapi的兼容性有些问题，改用自定义的AlibabaOpenAiChatModel进行修复
     * 多媒体为视频时，spring ai目前没有支持
     */
    public Flux<String> multiModalChat(String prompt, List<MultipartFile> files) {
        Media[] medias = files.stream()
                .map(file ->
                        new Media(MimeType.valueOf(Objects.requireNonNull(file.getContentType())), file.getResource())
                )
                .toArray(Media[]::new);
        return multiModalClient.prompt()
                .user(u->u.text( prompt).media(medias))
                .stream()
                .content();
    }

    /**
     * 上传文件
     * 模拟个人知识库
     */
    public void uploadFile(MultipartFile file, Integer chatId) {
        log.info("上传文件：{}", file.getOriginalFilename());
        Resource fileResource = file.getResource();
        map.put(chatId, fileResource);
        writeToVectorStore(fileResource);
    }

    //  读取、切割、向量化、存储到向量库
    private void writeToVectorStore(Resource resource) {
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
    }

    /**
     *  RAG 问答
     */
    public Flux<String> ragChat(String prompt, Integer chatId) {
        Resource resource = map.get(chatId);
        return ragChatClient.prompt()
                .user(u->u.text( prompt))
                .advisors(a->a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(a->a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, "file_name == '"+resource.getFilename()+"'"))
                .stream()
                .content();
    }

    /**
     * function calling  client
     * 设定一个体检机构的智能客服助手，可以为用户
     * 1、提供健康状况的判定
     * 2、体检套餐查询
     * 3、体检预约
     * 以上三个是基于function calling 进行实现
     */
    public String toolsChat(Integer chatId, String prompt){
        return toolsChatClient.prompt()
                .user(u->u.text( prompt))
                .advisors(a->a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .content();
    }

    /**
     * function calling  client
     * stream client
     */
    public Flux<String> toolsChatStream(Integer chatId, String prompt){
        return toolsChatStreamClient.prompt()
                .user(u->u.text( prompt))
                .advisors(a->a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .stream()
                .content();
    }

}