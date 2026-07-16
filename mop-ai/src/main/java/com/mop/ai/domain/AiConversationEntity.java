package com.mop.ai.domain;

import com.mop.common.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 对话会话实体
 * 对应数据库表 ai_conversation
 * 每条记录代表一个独立的对话会话，包含该会话的基本信息和状态
 * 继承 BaseEntity 获得 createBy、createTime、updateTime 等公共字段
 *
 * @author weiyiming
 */
@Schema(description = "AI对话会话")
public class AiConversationEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 会话主键 ID
     */
    @Schema(description = "会话ID", example = "1")
    private Long id;

    /**
     * 会话标题（首条消息自动截取前15字，也可手动重命名）
     */
    @Schema(description = "会话标题", example = "如何优化SQL查询")
    private String title;

    /**
     * 创建该会话的用户 ID，关联 sys_user 表
     */
    @Schema(description = "创建该会话的用户ID", example = "1")
    private Long userId;

    /**
     * 本次会话使用的 AI 模型名称
     * 例如：qwen-plus / qwen-turbo / deepseek-chat / gpt-4o-mini
     */
    @Schema(description = "AI模型名称", example = "ep-20260703164026-ncjmt")
    private String model;

    /**
     * 会话状态
     * 1 —— 正常
     * 0 —— 已删除（逻辑删除，不物理删除记录）
     */
    @Schema(description = "会话状态（1=正常 0=已删除）", allowableValues = {"0", "1"}, example = "1")
    private Integer status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
