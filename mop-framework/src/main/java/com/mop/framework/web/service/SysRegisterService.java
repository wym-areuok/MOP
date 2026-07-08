package com.mop.framework.web.service;

import com.mop.common.constant.CacheConstants;
import com.mop.common.constant.Constants;
import com.mop.common.constant.UserConstants;
import com.mop.common.core.domain.entity.SysUser;
import com.mop.common.core.domain.model.RegisterBody;
import com.mop.common.core.redis.RedisCache;
import com.mop.common.exception.user.CaptchaException;
import com.mop.common.exception.user.CaptchaExpireException;
import com.mop.common.utils.DateUtils;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.SecurityUtils;
import com.mop.common.utils.StringUtils;
import com.mop.framework.manager.AsyncManager;
import com.mop.framework.manager.factory.AsyncFactory;
import com.mop.system.service.ISysConfigService;
import com.mop.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 注册校验方法
 *
 * @author weiyiming
 */
@Component
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody) {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled) {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username)) {
            msg = MessageUtils.message("register.username.not.empty");
        } else if (StringUtils.isEmpty(password)) {
            msg = MessageUtils.message("register.password.not.empty");
        } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            msg = MessageUtils.message("register.username.length.invalid");
        } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            msg = MessageUtils.message("register.password.length.invalid");
        } else if (!userService.checkUserNameUnique(sysUser)) {
            msg = MessageUtils.message("register.username.exists", username);
        } else {
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag) {
                msg = MessageUtils.message("register.fail");
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));
            }
        }
        return msg;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException();
        }
    }
}
