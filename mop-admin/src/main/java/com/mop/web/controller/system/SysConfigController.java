package com.mop.web.controller.system;

import com.mop.common.annotation.Log;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.poi.ExcelUtil;
import com.mop.system.domain.SysConfig;
import com.mop.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author weiyiming
 */
@Tag(name = "参数管理")
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {
    @Autowired
    private ISysConfigService configService;

    @Operation(summary = "查询参数配置列表")
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysConfig config) {
        startPage();
        List<SysConfig> list = configService.selectConfigList(config);
        return getDataTable(list);
    }

    @Operation(summary = "导出参数配置")
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:config:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysConfig config) {
        List<SysConfig> list = configService.selectConfigList(config);
        ExcelUtil<SysConfig> util = new ExcelUtil<SysConfig>(SysConfig.class);
        util.exportExcel(response, list, MessageUtils.message("config.export.title"));
    }

    @Operation(summary = "根据参数ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @GetMapping(value = "/{configId}")
    public AjaxResult getInfo(@Parameter(description = "参数ID") @PathVariable Long configId) {
        return success(configService.selectConfigById(configId));
    }

    @Operation(summary = "根据参数键名查询参数值")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    @GetMapping(value = "/configKey/{configKey}")
    public AjaxResult getConfigKey(@Parameter(description = "参数键名") @PathVariable String configKey) {
        return success(configService.selectConfigByKey(configKey));
    }

    @Operation(summary = "新增参数配置")
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return error(MessageUtils.message("config.add.fail.key.exists", config.getConfigName()));
        }
        config.setCreateBy(getUsername());
        return toAjax(configService.insertConfig(config));
    }

    @Operation(summary = "修改参数配置")
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return error(MessageUtils.message("config.update.fail.key.exists", config.getConfigName()));
        }
        config.setUpdateBy(getUsername());
        return toAjax(configService.updateConfig(config));
    }

    @Operation(summary = "删除参数配置")
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@Parameter(description = "参数ID数组") @PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return success();
    }

    @Operation(summary = "刷新参数缓存")
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @PostMapping("/refreshCache")
    public AjaxResult refreshCache() {
        configService.resetConfigCache();
        return success();
    }
}
