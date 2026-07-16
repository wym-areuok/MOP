package com.mop.web.controller.monitor;

import com.mop.common.core.domain.AjaxResult;
import com.mop.framework.config.DynamicDsFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据源开关重载控制器
 * <p>
 * 运维通过字典管理页面修改 datasource_switch 后，
 * 调用此接口使配置立即生效，无需重启应用。
 *
 * @author weiyiming
 */
@Tag(name = "数据源管理")
@RestController
@RequestMapping("/monitor/datasource")
public class DatasourceReloadController {

    @Autowired
    private DynamicDsFilter dsFilter;

    @Operation(summary = "重新加载数据源开关配置")
    @PostMapping("/reload")
    @PreAuthorize("@ss.hasPermi('monitor:datasource:reload')")
    public AjaxResult reload() {
        dsFilter.refresh();
        return AjaxResult.success("数据源开关已刷新");
    }
}
