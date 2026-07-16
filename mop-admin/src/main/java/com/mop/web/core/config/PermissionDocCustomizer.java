package com.mop.web.core.config;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动提取 @PreAuthorize 中的权限标识，追加到 Knife4j 接口文档描述中。
 * <p>
 * 支持的 SpEL 表达式：
 * <ul>
 *   <li>{@code @ss.hasPermi('system:user:list')} → "system:user:list"</li>
 *   <li>{@code @ss.hasRole('admin')} → "ROLE:admin"</li>
 *   <li>类级 + 方法级注解叠加展示</li>
 * </ul>
 * <p>
 * 权限信息以 Markdown 格式追加到接口描述尾部，在 Knife4j 中自动渲染。
 *
 * @author weiyiming
 */
@Component
public class PermissionDocCustomizer implements OperationCustomizer {

    private static final Pattern HAS_PERMI = Pattern.compile("hasPermi\\('([^']+)'\\)");
    private static final Pattern HAS_ROLE = Pattern.compile("hasRole\\('([^']+)'\\)");

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Set<String> permissions = new LinkedHashSet<>();

        // 类级 @PreAuthorize（如 AiChatController 上的 ai:chat:list）
        PreAuthorize classAuth = handlerMethod.getBeanType().getAnnotation(PreAuthorize.class);
        if (classAuth != null) {
            extract(classAuth.value(), permissions);
        }

        // 方法级 @PreAuthorize
        PreAuthorize methodAuth = handlerMethod.getMethodAnnotation(PreAuthorize.class);
        if (methodAuth != null) {
            extract(methodAuth.value(), permissions);
        }

        if (!permissions.isEmpty()) {
            String current = operation.getDescription();
            StringBuilder sb = new StringBuilder();
            if (current != null && !current.isBlank()) {
                sb.append(current);
            }
            sb.append("\n\n**所需权限**: ");
            int i = 0;
            for (String p : permissions) {
                if (i++ > 0) sb.append("、");
                sb.append("`").append(p).append("`");
            }
            operation.setDescription(sb.toString());
        }

        return operation;
    }

    private void extract(String spel, Set<String> result) {
        Matcher m = HAS_PERMI.matcher(spel);
        while (m.find()) {
            result.add(m.group(1));
        }
        m = HAS_ROLE.matcher(spel);
        while (m.find()) {
            result.add("ROLE:" + m.group(1));
        }
    }
}
