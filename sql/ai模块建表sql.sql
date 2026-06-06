-- 1. AI 对话会话表
CREATE TABLE ai_conversation (
  id           BIGINT       NOT NULL  IDENTITY(1,1),
  title        NVARCHAR(100) COLLATE Chinese_Taiwan_Stroke_BIN NOT NULL  DEFAULT N'新对话',

CREATE INDEX idx_user_id ON ai_conversation (user_id);

-- 添加注释 (使用系统预定义的存储过程)
EXEC sp_set_table_comment 'ai_conversation', N'ai对话会话表';
EXEC sp_set_column_comment 'ai_conversation', 'id', N'会话id';
EXEC sp_set_column_comment 'ai_conversation', 'title', N'会话标题';
EXEC sp_set_column_comment 'ai_conversation', 'create_time', N'创建时间';
EXEC sp_set_column_comment 'ai_conversation', 'update_time', N'更新时间';

-- 2. AI 对话消息表
CREATE TABLE ai_message (
  id              BIGINT        NOT NULL IDENTITY(1,1),
  conversation_id BIGINT        NOT NULL,

CREATE INDEX idx_conversation_id ON ai_message (conversation_id);

-- 添加注释 (使用系统预定义的存储过程)
EXEC sp_set_table_comment 'ai_message', N'ai对话消息表';
EXEC sp_set_column_comment 'ai_message', 'id', N'消息id';
EXEC sp_set_column_comment 'ai_message', 'conversation_id', N'会话id';
EXEC sp_set_column_comment 'ai_message', 'tokens', N'消耗token数';
EXEC sp_set_column_comment 'ai_message', 'create_time', N'创建时间';

-- 3. AI 对话菜单插入
SET IDENTITY_INSERT sys_menu ON;
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (5, N'AI对话', 0, 0, N'aiChat', N'ai/chat', N'', N'', 1, 0, N'C', N'0', N'0', N'', N'user', N'admin', GETDATE(), N'', NULL, N'AI对话地址');