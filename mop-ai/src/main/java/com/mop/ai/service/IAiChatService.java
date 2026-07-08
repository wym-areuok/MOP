package com.mop.ai.service;

import com.mop.ai.domain.AiConversationEntity;
import com.mop.ai.domain.AiMessageEntity;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI 对话 Service 接口
 * 定义会话管理和流式对话的核心业务方法
 * 具体实现见 AiChatServiceImpl，底层模型由 AiModelConfig 工厂注入
 *
 * @author weiyiming
 */
public interface IAiChatService {
    /**
     * 新建会话
     * 标题默认为"新对话"，发送首条消息后自动更新为消息内容前15字
     *
     * @param userId 当前登录用户 ID
     * @param model  指定模型名称，传 null 时使用配置文件默认模型
     * @return 创建成功的会话实体（含自动回填的 id）
     */
    AiConversationEntity createConversation(Long userId, String model);

    /**
     * 查询当前用户的所有会话列表
     * 只返回未删除的会话，按最后更新时间倒序排列
     *
     * @param userId 当前登录用户 ID
     * @return 会话列表，无记录时返回空列表
     */
    List<AiConversationEntity> listConversations(Long userId);

    /**
     * 查询指定会话下的消息历史
     * 会先校验该会话是否属于当前用户，防止越权查看
     *
     * @param conversationId 会话 ID
     * @param userId         当前登录用户 ID
     * @return 消息列表（按时间正序），会话不存在或无权限时返回空列表
     */
    List<AiMessageEntity> listMessages(Long conversationId, Long userId);

    /**
     * 重命名会话标题
     * 会校验 userId，只能修改自己的会话
     *
     * @param id     会话 ID
     * @param title  新标题
     * @param userId 当前登录用户 ID
     * @return 影响行数
     */
    int renameConversation(Long id, String title, Long userId);

    /**
     * 删除会话（逻辑删除会话 + 物理删除该会话下所有消息）
     * 会校验 userId，只能删除自己的会话
     *
     * @param id     会话 ID
     * @param userId 当前登录用户 ID
     * @return 影响行数
     */
    int deleteConversation(Long id, Long userId);

    /**
     * 发送消息并以 SSE 流式返回 AI 回复
     * 方法内部异步执行，通过 SseEmitter 逐 token 推送给前端
     * 对话结束后自动将完整回复持久化到数据库
     *
     * @param conversationId 会话 ID
     * @param userMessage    用户输入的消息内容
     * @param userId         当前登录用户 ID（用于鉴权）
     * @param emitter        SSE 发射器，由 Controller 创建并传入
     */
    void chat(Long conversationId, String userMessage, Long userId, SseEmitter emitter);
}
