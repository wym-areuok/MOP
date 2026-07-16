package com.mop.quartz.controller;

import com.mop.common.annotation.Log;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.enums.BusinessType;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.poi.ExcelUtil;
import com.mop.quartz.domain.SysJobLog;
import com.mop.quartz.service.ISysJobLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度日志操作处理
 *
 * @author weiyiming
 */
@Tag(name = "定时任务日志")
@RestController
@RequestMapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {
    @Autowired
    private ISysJobLogService jobLogService;

    @Operation(summary = "查询定时任务调度日志列表")
    @PreAuthorize("@ss.hasPermi('monitor:job:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysJobLog sysJobLog) {
        startPage();
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        return getDataTable(list);
    }

    @Operation(summary = "导出定时任务调度日志")
    @PreAuthorize("@ss.hasPermi('monitor:job:export')")
    @Log(title = "任务调度日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJobLog sysJobLog) {
        List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
        ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
        util.exportExcel(response, list, MessageUtils.message("joblog.export.title"));
    }

    @Operation(summary = "根据日志ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('monitor:job:query')")
    @GetMapping(value = "/{jobLogId}")
    public AjaxResult getInfo(@Parameter(description = "日志ID") @PathVariable Long jobLogId) {
        return success(jobLogService.selectJobLogById(jobLogId));
    }


    @Operation(summary = "删除定时任务调度日志")
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "定时任务调度日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobLogIds}")
    public AjaxResult remove(@Parameter(description = "日志ID数组") @PathVariable Long[] jobLogIds) {
        return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
    }

    @Operation(summary = "清空所有定时任务调度日志")
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "调度日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean() {
        jobLogService.cleanJobLog();
        return success();
    }
}
