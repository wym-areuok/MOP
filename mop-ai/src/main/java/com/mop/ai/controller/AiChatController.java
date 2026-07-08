package com.mop.ai.controller;

import com.mop.ai.domain.AiConversationEntity;
import com.mop.ai.domain.AiMessageEntity;
import com.mop.ai.service.IAiChatService;
import com.mop.common.annotation.Log;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AI 对话 Controller
 * <p>
 * 职责：
 * 1. 提供 AI 对话页面的路由跳转
 * 2. 提供会话的增删改查 REST 接口（供前端 AJAX 调用）
 * 3. 提供 SSE 流式对话接口（供前端 EventSource 连接）
 * <p>
 * 接口路径前缀：/ai/chat
 * 页面模板路径：templates/ai/chat.html
 *
 * @author weiyiming
 */
@RestController
@RequestMapping("/ai/chat")
public class AiChatController extends BaseController {
    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private Executor aiTaskExecutor;

    /**
     * 跳转到 AI 对话主页面
     * GET /ai/chat
     *
     * @return Thymeleaf 模板路径 templates/ai/chat.html
     */
    @GetMapping()
    public String chatPage() {
        return "ai/chat";
    }

    /**
     * 获取当前登录用户的会话列表
     * GET /ai/chat/conversations
     *
     * @return 会话列表，按最后更新时间倒序，最多返回 50 条
     */
    @GetMapping("/conversations")
    public AjaxResult listConversations() {
        Long userId = SecurityUtils.getUserId();
        List<AiConversationEntity> list = aiChatService.listConversations(userId);
        return AjaxResult.success(list);
    }

    /**
     * 新建会话
     * POST /ai/chat/conversations
     *
     * @param model 指定使用的模型名称（可选），不传时使用 application.yml 配置的默认模型
     * @return 新建成功的会话实体（含 id、title、model 等字段）
     */
    @PostMapping("/conversations")
    public AjaxResult createConversation(@RequestParam(required = false) String model) {
        Long userId = SecurityUtils.getUserId();
        AiConversationEntity conv = aiChatService.createConversation(userId, model);
        return AjaxResult.success(conv);
    }

    /**
     * 重命名会话标题
     * PUT /ai/chat/conversations/{id}/title
     *
     * @param id    会话 ID（路径参数）
     * @param title 新标题（表单参数）
     * @return 操作结果
     */
    @PutMapping("/conversations/{id}/title")
    public AjaxResult renameConversation(@PathVariable Long id, @RequestParam String title) {
        Long userId = SecurityUtils.getUserId();
        aiChatService.renameConversation(id, title, userId);
        return AjaxResult.success();
    }

    /**
     * 删除会话（逻辑删除会话 + 物理删除该会话下所有消息）
     * DELETE /ai/chat/conversations/{id}
     * 操作记录写入操作日志
     *
     * @param id 会话 ID（路径参数）
     * @return 操作结果
     */
    @Log(title = "AI对话", businessType = BusinessType.DELETE)
    @DeleteMapping("/conversations/{id}")
    public AjaxResult deleteConversation(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        aiChatService.deleteConversation(id, userId);
        return AjaxResult.success();
    }

    /**
     * 获取指定会话的消息历史
     * GET /ai/chat/conversations/{id}/messages
     * 会校验会话归属，防止越权查看他人消息
     *
     * @param id 会话 ID（路径参数）
     * @return 消息列表，按时间正序，最多返回 100 条
     */
    @GetMapping("/conversations/{id}/messages")
    public AjaxResult listMessages(@PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        List<AiMessageEntity> messages = aiChatService.listMessages(id, userId);
        return AjaxResult.success(messages);
    }

    /**
     * 发送消息，以 SSE（Server-Sent Events）流式返回 AI 回复
     * GET /ai/chat/stream?conversationId=xxx&message=xxx
     * <p>
     * 工作原理：
     * 1. 创建 SseEmitter（timeout=0 不超时，由服务端在对话完成后主动关闭）
     * 2. 启动新线程异步调用 AI 接口，避免占用 Tomcat 线程池
     * 3. AI 每生成一个 token，通过 emitter 实时推送给前端
     * 4. 前端使用原生 EventSource API 接收，实现打字机效果
     * <p>
     * 前端监听的事件类型：
     * message —— 收到一个 token 片段
     * done    —— 流式输出完成
     * error   —— 服务端发生异常
     *
     * @param conversationId 会话 ID
     * @param message        用户输入的消息内容
     * @return SseEmitter 实例（Spring 自动将其转为 text/event-stream 响应）
     */
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream(@RequestParam Long conversationId, @RequestParam String message) {
        // 设置超时时间为0，表示永不超时，由服务端主动关闭
        final SseEmitter emitter = new SseEmitter(0L);
        Long userId = SecurityUtils.getUserId();
        // 捕获当前主线程的 SecurityContext
        final SecurityContext context = SecurityContextHolder.getContext();

        // 使用自定义线程池处理异步请求
        CompletableFuture.runAsync(() -> {
            try {
                // 为子线程设置安全上下文
                SecurityContextHolder.setContext(context);
                aiChatService.chat(conversationId, message, userId, emitter);
            } finally {
                // 执行完后必须清除，防止线程池污染
                SecurityContextHolder.clearContext();
            }
        }, aiTaskExecutor).exceptionally(ex -> {
            emitter.completeWithError(ex);
            return null;
        });

        return emitter;
    }
}