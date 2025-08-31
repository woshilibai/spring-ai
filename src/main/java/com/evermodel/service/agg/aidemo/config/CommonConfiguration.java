package com.evermodel.service.agg.aidemo.config;


import com.evermodel.service.agg.aidemo.model.AlibabaOpenAiChatModel;
import com.evermodel.service.agg.aidemo.tools.ChatTools;
import com.evermodel.service.agg.aidemo.tools.constants.PromptConstants;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Configuration
public class CommonConfiguration {

    // 注意参数中的model就是使用的模型，这里用了Ollama，也可以选择OpenAIChatModel
    @Bean
    public ChatClient chatClient(OllamaChatModel model) {
        return ChatClient.builder(model) // 创建ChatClient工厂
                .defaultSystem("你是一个热心、可爱的智能助手，你的名字叫小团团，请以小团团的身份和语气回答问题。")
                .defaultAdvisors(new SimpleLoggerAdvisor()) // 添加默认的Advisor,记录日志
                .build(); // 构建ChatClient实例

    }


    //  qwen-max 单模态会话大模型client
    @Bean
    public ChatClient qwChatClient(OpenAiChatModel model) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个 helpful 的助手，你的名字叫周小波，请以助手的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()      //  ai 日志记录器
                )
                .build();
    }

    /**
     * 会话记忆client
     */
    @Bean
    public ChatClient chatMemoryClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient
                .builder(model)
                .defaultSystem("你是一个 helpful 的助手，你的名字叫周小波，请以助手的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),      //  ai 日志记录器
                        new MessageChatMemoryAdvisor(chatMemory)    //  实现会话记忆
                )
                .build();
    }


    /**
     * 多模态模型client
     */
    @Bean
    public ChatClient multiModalClient(AlibabaOpenAiChatModel model) {
        return ChatClient
                .builder(model)
                .defaultOptions(ChatOptions.builder().model("qwen-omni-turbo").build())     //  指定模型，覆盖全局配置的模型
                .defaultSystem("你是一个 helpful 的助手，你的名字叫周小波，请以助手的身份和语气回答问题。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .build();
    }

    //  利用自定义的AlibabaOpenAiChatModel，解决千问模型对于openai部分兼容性问题
    @Bean
    public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
        String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.hasText(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }

        if (StringUtils.hasText(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(new SimpleApiKey(apiKey)).headers(CollectionUtils.toMultiValueMap(connectionHeaders)).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
        AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry) observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }

    /**
     * 会话记忆用内存型存储，也可以选择其他存储方式
     * @return
     */
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    /**
     * 向量存储
     * SimpleVectorStore 为内存型向量库，也可以选择其他存储方式
     * 向量库的作用：1、向量数据存储 2、基于相似度做向量检索
     */
    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    public ChatClient ragChatClient(OpenAiChatModel model,
                                    ChatMemory chatMemory,
                                    VectorStore vectorStore) {
        return ChatClient
                .builder(model)
                .defaultSystem("请根据提供的上下文回答问题，不要自己猜测。")
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),      //  ai 日志记录器
                        new MessageChatMemoryAdvisor(chatMemory),    //  实现会话记忆
                        new QuestionAnswerAdvisor(vectorStore,  //  向量库
                                SearchRequest.builder()     //  向量检索的请求参数
                                        .similarityThreshold(0.5d) // 相似度阈值
                                        .topK(2) // 返回的文档片段数量
                                        .build())   //  实现RAG
                )
                .build();
    }


    /**
     * function calling client
     */
    @Bean
    public ChatClient toolsChatClient(OpenAiChatModel model, ChatMemory chatMemory, ChatTools tools) {
        return ChatClient
                .builder(model)
                .defaultSystem(PromptConstants.USER_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),      //  ai 日志记录器
                        new MessageChatMemoryAdvisor(chatMemory)    //  实现会话记忆
                )
                .defaultTools( tools)
                .build();
    }

    /**
     * function calling client
     * 解决alibaba 千问模型流式回答的报错问题
     */
    @Bean
    public ChatClient toolsChatStreamClient(AlibabaOpenAiChatModel model, ChatMemory chatMemory, ChatTools tools) {
        return ChatClient
                .builder(model)
                .defaultSystem(PromptConstants.USER_PROMPT)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),      //  ai 日志记录器
                        new MessageChatMemoryAdvisor(chatMemory)    //  实现会话记忆
                )
                .defaultTools( tools)
                .build();
    }

}