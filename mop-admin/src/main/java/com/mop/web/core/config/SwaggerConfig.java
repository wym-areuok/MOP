package com.mop.web.core.config;

import com.mop.common.config.MopConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger2的接口配置
 *
 * @author weiyiming
 */
@Configuration
public class SwaggerConfig {
    /**
     * 系统基础配置
     */
    @Autowired
    private MopConfig mopConfig;

    /**
     * 自定义的 OpenAPI 对象
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI().components(new Components()
                        // 设置认证的请求头
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

    /**
     * 添加摘要信息
     */
    public Info getApiInfo() {
        return new Info()
                // 设置标题
                .title("MOP 管理系统_接口文档")
                // 描述
                .description("MES Operation Platform API 文档" +
                        "<br><br>说明：系统管理/监控管理/定时任务/代码生成/通用模块等接口不做统计" +
                        "<br>- 仅自行开发业务模块的接口会包含详细文档" +
                        "<br>- 后续新增的业务接口建议补充 Swagger 注解以提升文档可读性")
                // 作者信息
                .contact(new Contact().name("weiyiming"))
                // 版本
                .version(mopConfig.getVersion());
    }
}
