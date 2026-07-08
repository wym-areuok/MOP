package com.mop.web.controller.system;

import com.mop.common.config.MopConfig;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.domain.entity.SysUser;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.SecurityUtils;
import com.mop.common.utils.StringUtils;
import com.mop.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 首页
 *
 * @author weiyiming
 */
@RestController
public class SysIndexController {
    /**
     * 系统基础配置
     */
    @Autowired
    private MopConfig mopConfig;

    @Autowired
    private ISysUserService userService;

    /**
     * 访问首页，提示语
     */
    @RequestMapping("/")
    public String index() {
        return StringUtils.format(MessageUtils.message("sys.index.welcome"), mopConfig.getName(), mopConfig.getVersion());
    }

    /**
     * 解锁屏幕
     */
    @PostMapping("/unlockscreen")
    public AjaxResult unlockScreen(@RequestBody Map<String, String> body) {
        String password = body.get("password");
        if (StringUtils.isEmpty(password)) {
            return AjaxResult.error(MessageUtils.message("user.password.cannot.empty"));
        }
        String username = SecurityUtils.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        if (user == null) {
            return AjaxResult.error(MessageUtils.message("user.login.timeout"));
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
            return AjaxResult.error(MessageUtils.message("user.password.wrong"));
        }

        return AjaxResult.success(MessageUtils.message("user.unlock.success"));
    }
}
