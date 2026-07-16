package com.mop.web.core.config;

import com.mop.common.config.MopConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc + Knife4j 接口文档配置
 * <p>
 * 分组策略：按业务模块拆分 6 个 Group，编号前缀控制 Knife4j 下拉框排序。
 * 权限信息由 PermissionDocCustomizer 自动提取 @PreAuthorize 注入文档。
 * 响应体 AjaxResult/TableDataInfo 由 AjaxResultResponseCustomizer 统一处理。
 * <p>
 * 在线调试：右上角 Authorize 填入 Bearer Token 后即可调试接口。
 *
 * @author weiyiming
 */
@Configuration
public class SwaggerConfig {

    @Autowired
    private MopConfig mopConfig;

    // ===================== OpenAPI 全局配置 =====================

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("apikey", securityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("apikey"))
                .info(getApiInfo());
    }

    @Bean
    public SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .name("Authorization")
                .in(SecurityScheme.In.HEADER)
                .scheme("Bearer");
    }

    private Info getApiInfo() {
        return new Info()
                .title("MOP 管理系统_接口文档")
                .description("MES Operation Platform API 文档"
                        + "<br><br>**分组说明**："
                        + "<br>· 1-系统管理：用户 / 角色 / 菜单 / 部门 / 岗位 / 字典 / 参数 / 公告 / 登录 / 注册 / 个人中心"
                        + "<br>· 2-系统监控：服务监控 / 缓存管理 / 在线用户 / 操作日志 / 登录日志"
                        + "<br>· 3-定时任务：任务管理 / 任务日志"
                        + "<br>· 4-AI对话：会话管理 / 流式对话"
                        + "<br>· 5-系统工具：代码生成"
                        + "<br>· 6-通用接口：文件上传 / 下载 / 验证码"
                        + "<br><br>**在线调试**：点击右上角 Authorize 填入登录返回的 Token 后即可调试接口。"
                        + "<br>**字段说明**：所有实体字段描述与数据库 mop_initial.sql 中的 MS_Description 保持一致。")
                .contact(new Contact().name("weiyiming"))
                .version(mopConfig.getVersion());
    }

    // 共享的 Info 自定义器，确保每个分组都能显示 title/version/description
    private OpenApiCustomizer infoCustomizer() {
        return openApi -> openApi.info(getApiInfo());
    }

    // ===================== 分组定义 =====================

    @Bean
    public GroupedOpenApi systemGroup() {
        return GroupedOpenApi.builder()
                .group("1-system")
                .displayName("1-系统管理")
                .packagesToScan("com.mop.web.controller.system")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi monitorGroup() {
        return GroupedOpenApi.builder()
                .group("2-monitor")
                .displayName("2-系统监控")
                .packagesToScan("com.mop.web.controller.monitor")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi quartzGroup() {
        return GroupedOpenApi.builder()
                .group("3-quartz")
                .displayName("3-定时任务")
                .packagesToScan("com.mop.quartz.controller")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi aiGroup() {
        return GroupedOpenApi.builder()
                .group("4-ai")
                .displayName("4-AI对话")
                .packagesToScan("com.mop.ai.controller")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi toolGroup() {
        return GroupedOpenApi.builder()
                .group("5-tool")
                .displayName("5-系统工具")
                .packagesToScan("com.mop.generator.controller")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }

    @Bean
    public GroupedOpenApi commonGroup() {
        return GroupedOpenApi.builder()
                .group("6-common")
                .displayName("6-通用接口")
                .packagesToScan("com.mop.web.controller.common")
                .addOpenApiCustomizer(infoCustomizer())
                .build();
    }
}
