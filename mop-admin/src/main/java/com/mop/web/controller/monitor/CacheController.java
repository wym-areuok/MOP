package com.mop.web.controller.monitor;

import com.mop.common.constant.CacheConstants;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.StringUtils;
import com.mop.system.domain.SysCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 缓存监控
 *
 * @author weiyiming
 */
@Tag(name = "缓存管理")
@RestController
@RequestMapping("/monitor/cache")
public class CacheController {
    private final static List<SysCache> caches = new ArrayList<SysCache>();
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    private void initCacheNames() {
        caches.add(new SysCache(CacheConstants.LOGIN_TOKEN_KEY, MessageUtils.message("cache.name.user_info")));
        caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, MessageUtils.message("cache.name.config")));
        caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, MessageUtils.message("cache.name.dict")));
        caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, MessageUtils.message("cache.name.captcha")));
        caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, MessageUtils.message("cache.name.repeat_submit")));
        caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, MessageUtils.message("cache.name.rate_limit")));
        caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, MessageUtils.message("cache.name.pwd_err_cnt")));
    }

    @SuppressWarnings("deprecation")
    @Operation(summary = "获取 Redis 基本信息（info/commandstats/dbSize）")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping()
    public AjaxResult getInfo() throws Exception {
        Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info());
        Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
        Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.dbSize());

        Map<String, Object> result = new HashMap<>(3);
        result.put("info", info);
        result.put("dbSize", dbSize);

        List<Map<String, String>> pieList = new ArrayList<>();
        commandStats.stringPropertyNames().forEach(key -> {
            Map<String, String> data = new HashMap<>(2);
            String property = commandStats.getProperty(key);
            data.put("name", StringUtils.removeStart(key, "cmdstat_"));
            data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
            pieList.add(data);
        });
        result.put("commandStats", pieList);
        return AjaxResult.success(result);
    }

    @Operation(summary = "获取缓存名称列表")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getNames")
    public AjaxResult cache() {
        return AjaxResult.success(caches);
    }

    @Operation(summary = "获取指定缓存的所有 Key")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getKeys/{cacheName}")
    public AjaxResult getCacheKeys(@Parameter(description = "缓存名称前缀") @PathVariable String cacheName) {
        Set<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        return AjaxResult.success(new TreeSet<>(cacheKeys));
    }

    @Operation(summary = "获取指定缓存 Key 的值")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    public AjaxResult getCacheValue(
            @Parameter(description = "缓存名称") @PathVariable String cacheName,
            @Parameter(description = "缓存 Key") @PathVariable String cacheKey) {
        String cacheValue = redisTemplate.opsForValue().get(cacheKey);
        SysCache sysCache = new SysCache(cacheName, cacheKey, cacheValue);
        return AjaxResult.success(sysCache);
    }

    @Operation(summary = "按名称前缀清空缓存")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheName/{cacheName}")
    public AjaxResult clearCacheName(@Parameter(description = "缓存名称前缀") @PathVariable String cacheName) {
        Collection<String> cacheKeys = redisTemplate.keys(cacheName + "*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }

    @Operation(summary = "清空指定缓存 Key")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheKey/{cacheKey}")
    public AjaxResult clearCacheKey(@Parameter(description = "缓存 Key") @PathVariable String cacheKey) {
        redisTemplate.delete(cacheKey);
        return AjaxResult.success();
    }

    @Operation(summary = "清空所有缓存")
    @PreAuthorize("@ss.hasPermi('monitor:cache:list')")
    @DeleteMapping("/clearCacheAll")
    public AjaxResult clearCacheAll() {
        // WARNING: redisTemplate.keys("*") 在生产环境会阻塞 Redis！
        // 仅应在维护窗口或内部环境使用，严禁在生产高峰期调用。
        Collection<String> cacheKeys = redisTemplate.keys("*");
        redisTemplate.delete(cacheKeys);
        return AjaxResult.success();
    }
}
