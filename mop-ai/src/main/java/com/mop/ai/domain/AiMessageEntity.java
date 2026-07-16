package com.mop.ai.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * AI 对话消息实体
 * 对应数据库表 ai_message
 * 每条记录代表一次对话中的单条消息（用户发送或 AI 回复）
 *
 * @author weiyiming
 */
@Schema(description = "AI对话消息")
public class AiMessageEntity {
    /**
     * 消息主键 ID
     */
    @Schema(description = "消息ID", example = "1")
    private Long id;

    /**
     * 所属会话 ID，关联 ai_conversation 表
     */
    @Schema(description = "所属会话ID", example = "1")
    private Long conversationId;

    /**
     * 消息角色
     * user      —— 用户发送的消息
     * assistant —— AI 回复的消息
     */
    @Schema(description = "消息角色", allowableValues = {"user", "assistant"}, example = "user")
    private String role;

    /**
     * 消息正文内容（支持 Markdown 格式）
     */
    @Schema(description = "消息正文内容（支持Markdown格式）", example = "你好，请问你能做什么？")
    private String content;

    /**
     * 本条消息消耗的 Token 数量（AI 回复时记录，用户消息为 0）
     */
    @Schema(description = "Token消耗量", example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer tokens;

    /**
     * 消息创建时间
     */
    @Schema(description = "消息创建时间", accessMode = Schema.AccessMode.READ_ONLY)
    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokens() {
        return tokens;
    }

    public void setTokens(Integer tokens) {
        this.tokens = tokens;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
