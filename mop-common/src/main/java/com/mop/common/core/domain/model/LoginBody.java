package com.mop.common.core.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户登录对象
 *
 * @author weiyiming
 */
@Schema(description = "登录请求体")
public class LoginBody {
    /**
     * 用户名
     */
    @Schema(description = "用户名", required = true, example = "admin")
    private String username;

    /**
     * 用户密码
     */
    @Schema(description = "用户密码", required = true, example = "admin123")
    private String password;

    /**
     * 验证码
     */
    @Schema(description = "验证码", example = "1234")
    private String code;

    /**
     * 唯一标识
     */
    @Schema(description = "验证码唯一标识", example = "abcd1234")
    private String uuid;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
