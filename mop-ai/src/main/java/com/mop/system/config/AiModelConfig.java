package com.mop.system.config;

import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

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
