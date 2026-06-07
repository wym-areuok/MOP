package com.mop.system.service.impl;

import com.mop.common.utils.SecurityUtils;
import com.mop.system.config.AiModelProperties;
import com.mop.system.domain.AiConversationEntity;
import com.mop.system.domain.AiMessageEntity;
import com.mop.system.mapper.AiChatMapper;
import com.mop.system.service.IAiChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 对话 Service 实现类 —— 基于 LangChain4j
 * <p>
 * 核心设计思路： 业务代码只依赖 LangChain4j 的 StreamingChatLanguageModel 接口， 不感知底层是哪个厂商的模型。 切换模型（通义千问 / DeepSeek / OpenAI / 本地
 * Ollama） 只需修改 application.yml 中的 ai.model.provider，本类代码零改动。
 *
 * @author ruoyi
 */
@Service
public class AiChatServiceImpl implements IAiChatService {
    private static final Logger log = LoggerFactory.getLogger(AiChatServiceImpl.class);

    /**
     * 流式聊天语言模型
     * 由 AiModelConfig 工厂 Bean 根据 application.yml 中的 provider 配置动态注入，
     * 可能是 OpenAiStreamingChatModel / OllamaStreamingChatModel 等具体实现
     */
    @Autowired
    private StreamingChatLanguageModel streamingModel;

    /**
     * AI 模型配置属性（模型名称、温度、最大 Token、系统提示词等）
     */
    @Autowired
    private AiModelProperties modelProps;

    @Autowired
    private AiChatMapper aiChatMapper;

    // ----------------------------------------------------------------
    // 会话管理
    // ----------------------------------------------------------------

    /**
     * 新建会话 model 参数为空时，取 application.yml 中配置的默认模型名
     */
    @Override
    public AiConversationEntity createConversation(Long userId, String model) {
        AiConversationEntity conv = new AiConversationEntity();
        conv.setUserId(userId);
        conv.setTitle("新对话");
        conv.setModel(model == null ? modelProps.getModelName() : model);
        conv.setCreateBy(SecurityUtils.getLoginUser().getUser().getUserName());
        aiChatMapper.insertConversation(conv);
        return conv;
    }

    @Override
    public List<AiConversationEntity> listConversations(Long userId) {
        List<AiConversationEntity> list = aiChatMapper.selectConversationsByUserId(userId);
        // 防御性处理：MyBatis 查无数据时可能返回 null，统一转为空列表
        return list != null ? list : new ArrayList<>();
    }

    /**
     * 查询会话消息
     * 先鉴权（校验会话是否属于当前用户），再查消息，防止越权访问
     */
    @Override
    public List<AiMessageEntity> listMessages(Long conversationId, Long userId) {
        AiConversationEntity conv = aiChatMapper.selectConversationById(conversationId, userId);
        if (conv == null) {
            return new ArrayList<>();
        }
        // 查询历史记录时，默认展示最近 100 条
        return aiChatMapper.selectMessagesByConversationId(conversationId, 100);
    }

    @Override
    public int renameConversation(Long id, String title, Long userId) {
        return aiChatMapper.updateConversationTitle(id, title, userId);
    }

    /**
     * 删除会话 先物理删除该会话下所有消息，再逻辑删除会话本身
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteConversation(Long id, Long userId) {
        aiChatMapper.deleteMessagesByConversationId(id);
        return aiChatMapper.deleteConversation(id, userId);
    }

    // ----------------------------------------------------------------
    // 核心：LangChain4j 流式对话
    // ----------------------------------------------------------------

    /**
     * 发送消息，流式返回 AI 回复
     * <p>
     * 完整流程：
     * 1. 鉴权 —— 校验会话归属
     * 2. 持久化用户消息
     * 3. 首条消息自动命名会话标题
     * 4. 从 DB 加载历史消息并构建 LangChain4j 消息列表
     * 5. 调用流式模型，逐 token 通过 SSE 推送给前端
     * 6. 流式结束后持久化完整 AI 回复及 Token 消耗
     */
    @Override
    public void chat(Long conversationId, String userInput, Long userId, SseEmitter emitter) {
        // 1. 鉴权：会话必须属于当前用户
        AiConversationEntity conv = aiChatMapper.selectConversationById(conversationId, userId);
        if (conv == null) {
            sendSse(emitter, "error", "会话不存在或无权限");
            return;
        }

        // 2. 持久化用户消息（先存库，再发给 AI，保证消息不丢失）
        AiMessageEntity userMsg = new AiMessageEntity();
        userMsg.setConversationId(conversationId);
        userMsg.setRole("user");
        userMsg.setContent(userInput);
        aiChatMapper.insertMessage(userMsg);

        // 3. 首条消息自动命名会话标题（截取前15字 + 省略号）
        if ("新对话".equals(conv.getTitle())) {
            String autoTitle = userInput.length() > 15 ? userInput.substring(0, 15) + "…" : userInput;
            aiChatMapper.updateConversationTitle(conversationId, autoTitle, userId);
        }

        // 4. 构建完整的消息上下文（SystemMessage + 历史消息）
        List<ChatMessage> messages = buildMessages(conversationId);

        // 5. 调用 LangChain4j 流式接口（回调在 LangChain4j 内部线程执行，不阻塞当前线程）
        StringBuilder fullReply = new StringBuilder();

        streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
            /**
             * 每收到一个 token 片段时触发
             * 累积到 fullReply 的同时，实时通过 SSE 推送给前端实现打字机效果
             */
            @Override
            public void onNext(String token) {
                fullReply.append(token);
                sendSse(emitter, "message", token);
            }

            /**
             * 流式输出全部完成时触发
             * 将完整回复持久化到数据库，并通知前端对话结束
             */
            @Override
            public void onComplete(Response<AiMessage> response) {
                // 6. 持久化 AI 完整回复
                if (fullReply.length() > 0) {
                    AiMessageEntity aiMsg = new AiMessageEntity();
                    aiMsg.setConversationId(conversationId);
                    aiMsg.setRole("assistant");
                    aiMsg.setContent(fullReply.toString());
                    // 记录本次对话消耗的 Token 总数（用于统计和计费）
                    if (response.tokenUsage() != null) {
                        aiMsg.setTokens(response.tokenUsage().totalTokenCount());
                    }
                    aiChatMapper.insertMessage(aiMsg);
                }
                // 通知前端流式结束，前端收到后关闭 EventSource
                sendSse(emitter, "done", "[DONE]");
                emitter.complete();
            }

            /**
             * 调用过程中发生异常时触发（网络超时、模型服务异常等）
             */
            @Override
            public void onError(Throwable error) {
                log.error("LangChain4j 流式调用异常", error);
                sendSse(emitter, "error", "AI 服务异常(请检查余额或配置)：" + error.getMessage());
                emitter.completeWithError(error);
            }
        });
    }

    // ----------------------------------------------------------------
    // 私有工具方法
    // ----------------------------------------------------------------

    /**
     * 构建发送给 AI 的完整消息列表
     * <p>
     * 结构：[SystemMessage] + [历史 user/assistant 消息（最近 N 条）]
     * <p>
     * SystemMessage 来自 application.yml 的 ai.model.system-prompt，
     * 修改配置后重启即可生效，无需改代码。
     * <p>
     * 历史消息数量由 ai.model.max-history-messages 控制，
     * 超出时从最早的消息开始裁剪，保留最近 N 条，防止超出模型 context window。
     *
     * @param conversationId 会话 ID
     * @return LangChain4j 格式的消息列表
     */
    private List<ChatMessage> buildMessages(Long conversationId) {
        int max = modelProps.getMaxHistoryMessages();
        // 直接从数据库查最近的 N 条，减少内存负载
        List<AiMessageEntity> history = aiChatMapper.selectMessagesByConversationId(conversationId, max);

        List<ChatMessage> list = new ArrayList<>();

        // 插入系统提示词（定义 AI 的角色、行为规范）
        String system = modelProps.getSystemPrompt();
        if (system != null && !system.isEmpty()) {
            log.debug(">>> 注入系统提示词: {}", system);
            list.add(SystemMessage.from(system));
        }

        // 既然 SQL 已经限制了条数，直接添加即可
        for (AiMessageEntity m : history) {
            if ("user".equals(m.getRole())) {
                list.add(UserMessage.from(m.getContent()));
            } else {
                // assistant 消息转为 LangChain4j 的 AiMessage 类型
                list.add(AiMessage.from(m.getContent()));
            }
        }
        return list;
    }

    /**
     * 向前端安全推送一条 SSE 事件
     * 捕获异常防止因客户端断开连接而导致整个线程崩溃
     *
     * @param emitter SSE 发射器
     * @param event   事件名称（message / done / error）
     * @param data    事件数据
     */
    private void sendSse(SseEmitter emitter, String event, String data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (Exception e) {
            log.warn("SSE 推送失败，客户端可能已断开连接: event={}", event);
        }
    }
}