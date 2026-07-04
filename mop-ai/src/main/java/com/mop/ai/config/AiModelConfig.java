package com.mop.ai.config;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * AI 模型工厂
 * <p>
 * 根据 ai.model.provider 自动创建对应的 StreamingChatLanguageModel Bean。
 * 切换模型只需修改 application.yml，无需改动任何业务代码。
 * <p>
 * 支持的 provider：
 * dashscope  —— 阿里云通义千问（OpenAI 兼容接口）
 * openai     —— OpenAI 官方
 * deepseek   —— DeepSeek（OpenAI 兼容接口）
 * ollama     —— 本地 Ollama（完全免费）
 *
 * @author ruoyi
 */
@Configuration
public class AiModelConfig {
    private static final Logger log = LoggerFactory.getLogger(AiModelConfig.class);

    // DashScope OpenAI 兼容接口地址
    private static final String DASHSCOPE_BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    // DeepSeek OpenAI 兼容接口地址
    private static final String DEEPSEEK_BASE_URL = "https://api.deepseek.com/v1";

    @Autowired
    private AiModelProperties props;

    /**
     * AI 对话专用线程池
     * 隔离 I/O 密集型任务，避免影响系统主业务
     */
    @Bean(name = "aiTaskExecutor")
    public Executor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-chat-");
        // 关键：当线程池满时，由调用者线程处理，起到背压作用
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        String provider = props.getProvider();
        log.info(">>> 初始化 AI 模型，provider={}, model={}", provider, props.getModelName());

        switch (provider.toLowerCase()) {

            // ---- 阿里云通义千问 ----
            case "dashscope":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(DASHSCOPE_BASE_URL)
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();

            // ---- OpenAI 官方 ----
            case "openai":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(props.getBaseUrl())
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();

            // ---- DeepSeek ----
            case "deepseek":
                return OpenAiStreamingChatModel.builder()
                        .baseUrl(DEEPSEEK_BASE_URL)
                        .apiKey(props.getApiKey())
                        .modelName(props.getModelName())
                        .maxTokens(props.getMaxTokens())
                        .temperature(props.getTemperature())
                        .timeout(Duration.ofSeconds(120))
                        .build();

            // ---- 本地 Ollama ----
//            case "ollama":
//                return OllamaStreamingChatModel.builder()
//                        .baseUrl(props.getBaseUrl())
//                        .modelName(props.getModelName())
//                        .temperature(props.getTemperature())
//                        .timeout(Duration.ofSeconds(180))
//                        .build();

            default:
                throw new IllegalArgumentException(
                        "不支持的 AI provider: " + provider
                                + "，可选值: dashscope / openai / deepseek / ollama");
        }
    }
}
