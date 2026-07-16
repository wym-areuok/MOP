package com.mop.web.core.config;

import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

/**
 * 修正 AjaxResult (extends HashMap) 返回值在 Knife4j 中的 Schema 展示。
 * <p>
 * 背景：{@code AjaxResult extends HashMap<String,Object>}，
 * SpringDoc 无法从 HashMap 推断具体字段结构，导致响应体显示为 {}。
 * 本 Customizer 在文档构建阶段自动为返回 AjaxResult 的接口补充
 * {@code {code, msg, data}} 包装结构。
 * <p>
 * TableDataInfo 是普通 POJO（有 getter/setter），SpringDoc 能自动提取字段，无需处理。
 *
 * @author weiyiming
 */
@Component
public class AjaxResultResponseCustomizer implements OperationCustomizer {

    private static final Schema<?> GENERIC_SCHEMA;

    static {
        GENERIC_SCHEMA = new Schema<>()
                .type("object")
                .addProperty("code", new IntegerSchema()
                        .example(200)
                        .description("状态码，200=成功"))
                .addProperty("msg", new StringSchema()
                        .example("操作成功")
                        .description("提示消息"))
                .addProperty("data", new Schema<>()
                        .type("object")
                        .description("响应数据"));
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Class<?> returnType = handlerMethod.getReturnType().getParameterType();

        // 只处理返回 AjaxResult 的方法
        if (!AjaxResult.class.isAssignableFrom(returnType)) {
            return operation;
        }

        // TableDataInfo 虽然是 POJO 可从 getter 推断，但它继承自 AjaxResult 检查
        // 这里不做二次包装
        if (TableDataInfo.class.isAssignableFrom(returnType)) {
            return operation;
        }

        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            return operation;
        }

        ApiResponse ok = responses.get("200");
        if (ok == null) {
            return operation;
        }

        Content content = ok.getContent();
        if (content == null) {
            return operation;
        }

        MediaType json = content.get("application/json");
        if (json == null) {
            return operation;
        }

        Schema<?> original = json.getSchema();
        if (original == null) {
            return operation;
        }

        // 情况1：已有 $ref（通过 @ApiResponse 手动指定了 implementation）
        //        包装为 {code, msg, data: <$ref>}
        if (original.get$ref() != null && !original.get$ref().isEmpty()) {
            Schema<?> wrapper = new Schema<>()
                    .type("object")
                    .addProperty("code", new IntegerSchema().example(200).description("状态码"))
                    .addProperty("msg", new StringSchema().example("操作成功").description("提示消息"))
                    .addProperty("data", original.description("响应数据"));
            json.setSchema(wrapper);
            return operation;
        }

        // 情况2：HashMap 推断的空 schema → 替换为通用 AjaxResult 结构
        if (original.getProperties() == null || original.getProperties().isEmpty()) {
            json.setSchema(GENERIC_SCHEMA);
        }

        return operation;
    }
}
