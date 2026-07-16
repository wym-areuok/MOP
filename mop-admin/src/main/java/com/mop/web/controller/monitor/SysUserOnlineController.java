package com.mop.web.controller.monitor;

import com.mop.common.annotation.Log;
import com.mop.common.constant.CacheConstants;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.domain.model.LoginUser;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.core.redis.RedisCache;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.StringUtils;
import com.mop.system.domain.SysUserOnline;
import com.mop.system.service.ISysUserOnlineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 在线用户监控
 *
 * @author weiyiming
 */
@Tag(name = "在线用户")
@RestController
@RequestMapping("/monitor/online")
public class SysUserOnlineController extends BaseController {
    @Autowired
    private ISysUserOnlineService userOnlineService;

    @Autowired
    private RedisCache redisCache;

    @Operation(summary = "查询在线用户列表")
    @PreAuthorize("@ss.hasPermi('monitor:online:list')")
    @GetMapping("/list")
    public TableDataInfo list(
            @Parameter(description = "IP地址（可选）") String ipaddr,
            @Parameter(description = "用户名（可选）") String userName) {
        // NOTE: 使用 KEYS 命令扫描特定前缀的 key，仅扫描 login_tokens: 开头的少量 key。
        // 生产环境若在线用户数超过 10000+，建议改用 SCAN 命令或维护在线用户 Set 集合。
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> userOnlineList = new ArrayList<SysUserOnline>();
        for (String key : keys) {
            LoginUser user = redisCache.getCacheObject(key);
            if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, user));
            } else if (StringUtils.isNotEmpty(ipaddr)) {
                userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, user));
            } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(user.getUser())) {
                userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, user));
            } else {
                userOnlineList.add(userOnlineService.loginUserToUserOnline(user));
            }
        }
        Collections.reverse(userOnlineList);
        userOnlineList.removeAll(Collections.singleton(null));
        return getDataTable(userOnlineList);
    }

    @Operation(summary = "强退指定在线用户")
    @PreAuthorize("@ss.hasPermi('monitor:online:forceLogout')")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @DeleteMapping("/{tokenId}")
    public AjaxResult forceLogout(@Parameter(description = "用户Token ID") @PathVariable String tokenId) {
        userOnlineService.forceLogout(tokenId);
        return success();
    }
}
