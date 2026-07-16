package com.mop.ai.controller;

import com.mop.ai.domain.AiConversationEntity;
import com.mop.ai.domain.AiMessageEntity;
import com.mop.ai.service.IAiChatService;
import com.mop.common.annotation.Log;
import com.mop.common.annotation.RateLimiter;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.enums.BusinessType;
import com.mop.common.exception.ServiceException;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@Tag(name = "AI对话")
@RestController
@RequestMapping("/ai/chat")
@PreAuthorize("@ss.hasPermi('ai:chat:list')")
public class AiChatController extends BaseController {
    @Autowired
    private IAiChatService aiChatService;

    @Autowired
    private Executor aiTaskExecutor;

    @Operation(summary = "获取 AI 对话页面标识（返回模板路径供前端渲染）")
    @GetMapping()
    public String chatPage() {
        return "ai/chat";
    }

    @Operation(summary = "获取会话列表（最多50条，按最后更新时间倒序）")
    @GetMapping("/conversations")
    public AjaxResult listConversations() {
        Long userId = SecurityUtils.getUserId();
        List<AiConversationEntity> list = aiChatService.listConversations(userId);
        return AjaxResult.success(list);
    }

    @Operation(summary = "新建会话")
    @PostMapping("/conversations")
    public AjaxResult createConversation(@Parameter(description = "模型名称（可选，不传使用默认模型）") @RequestParam(required = false) String model) {
        Long userId = SecurityUtils.getUserId();
        AiConversationEntity conv = aiChatService.createConversation(userId, model);
        return AjaxResult.success(conv);
    }

    @Operation(summary = "重命名会话标题")
    @PutMapping("/conversations/{id}/title")
    public AjaxResult renameConversation(
            @Parameter(description = "会话ID") @PathVariable Long id,
            @Parameter(description = "新标题") @RequestParam String title) {
        Long userId = SecurityUtils.getUserId();
        aiChatService.renameConversation(id, title, userId);
        return AjaxResult.success();
    }

    @Operation(summary = "删除会话（逻辑删除+物理删除消息）")
    @Log(title = "AI对话", businessType = BusinessType.DELETE)
    @DeleteMapping("/conversations/{id}")
    public AjaxResult deleteConversation(@Parameter(description = "会话ID") @PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        aiChatService.deleteConversation(id, userId);
        return AjaxResult.success();
    }

    @Operation(summary = "获取会话消息历史（最多100条，按时间正序）")
    @GetMapping("/conversations/{id}/messages")
    public AjaxResult listMessages(@Parameter(description = "会话ID") @PathVariable Long id) {
        Long userId = SecurityUtils.getUserId();
        List<AiMessageEntity> messages = aiChatService.listMessages(id, userId);
        return AjaxResult.success(messages);
    }

    @Operation(summary = "SSE流式对话（异步推送，超时300秒）")
    @RateLimiter(key = "ai-chat", count = 10, time = 60)
    @GetMapping(value = "/stream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter stream(
            @Parameter(description = "会话ID") @RequestParam Long conversationId,
            @Parameter(description = "消息内容（最长4000字符）") @RequestParam String message) {
        // 输入校验：防止空消息和超长消息消耗 Token
        if (message == null || message.trim().isEmpty()) {
            throw new ServiceException(MessageUtils.message("ai.message.empty"));
        }
        if (message.length() > 4000) {
            throw new ServiceException(MessageUtils.message("ai.message.too.long"));
        }
        // 设置超时时间为 300 秒（5 分钟），由服务端主动关闭或超时自动关闭
        final SseEmitter emitter = new SseEmitter(300000L);
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