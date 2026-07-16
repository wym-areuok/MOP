package com.mop.quartz.controller;

import com.mop.common.annotation.Log;
import com.mop.common.constant.Constants;
import com.mop.common.core.controller.BaseController;
import com.mop.common.core.domain.AjaxResult;
import com.mop.common.core.page.TableDataInfo;
import com.mop.common.enums.BusinessType;
import com.mop.common.exception.job.TaskException;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.StringUtils;
import com.mop.common.utils.poi.ExcelUtil;
import com.mop.quartz.domain.SysJob;
import com.mop.quartz.service.ISysJobService;
import com.mop.quartz.util.CronUtils;
import com.mop.quartz.util.ScheduleUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 调度任务信息操作处理
 *
 * @author weiyiming
 */
@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/monitor/job")
public class SysJobController extends BaseController {
    @Autowired
    private ISysJobService jobService;

    @Operation(summary = "查询定时任务列表")
    @PreAuthorize("@ss.hasPermi('monitor:job:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysJob sysJob) {
        startPage();
        List<SysJob> list = jobService.selectJobList(sysJob);
        return getDataTable(list);
    }

    @Operation(summary = "导出定时任务数据")
    @PreAuthorize("@ss.hasPermi('monitor:job:export')")
    @Log(title = "定时任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysJob sysJob) {
        List<SysJob> list = jobService.selectJobList(sysJob);
        ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
        util.exportExcel(response, list, MessageUtils.message("job.export.title"));
    }

    @Operation(summary = "根据任务ID获取详细信息")
    @PreAuthorize("@ss.hasPermi('monitor:job:query')")
    @GetMapping(value = "/{jobId}")
    public AjaxResult getInfo(@Parameter(description = "任务ID") @PathVariable("jobId") Long jobId) {
        return success(jobService.selectJobById(jobId));
    }

    @Operation(summary = "新增定时任务")
    @PreAuthorize("@ss.hasPermi('monitor:job:add')")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysJob job) throws SchedulerException, TaskException {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return error(MessageUtils.message("job.cron.invalid", MessageUtils.message("common.add"), job.getJobName()));
        } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            return error(MessageUtils.message("job.target.rmi", MessageUtils.message("common.add"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
            return error(MessageUtils.message("job.target.ldap", MessageUtils.message("common.add"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
            return error(MessageUtils.message("job.target.http", MessageUtils.message("common.add"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            return error(MessageUtils.message("job.target.illegal", MessageUtils.message("common.add"), job.getJobName()));
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return error(MessageUtils.message("job.target.not.whitelist", MessageUtils.message("common.add"), job.getJobName()));
        }
        job.setCreateBy(getUsername());
        return toAjax(jobService.insertJob(job));
    }

    @Operation(summary = "修改定时任务")
    @PreAuthorize("@ss.hasPermi('monitor:job:edit')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
        if (!CronUtils.isValid(job.getCronExpression())) {
            return error(MessageUtils.message("job.cron.invalid", MessageUtils.message("common.edit"), job.getJobName()));
        } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            return error(MessageUtils.message("job.target.rmi", MessageUtils.message("common.edit"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
            return error(MessageUtils.message("job.target.ldap", MessageUtils.message("common.edit"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
            return error(MessageUtils.message("job.target.http", MessageUtils.message("common.edit"), job.getJobName()));
        } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            return error(MessageUtils.message("job.target.illegal", MessageUtils.message("common.edit"), job.getJobName()));
        } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            return error(MessageUtils.message("job.target.not.whitelist", MessageUtils.message("common.edit"), job.getJobName()));
        }
        job.setUpdateBy(getUsername());
        return toAjax(jobService.updateJob(job));
    }

    @Operation(summary = "修改定时任务状态（暂停/恢复）")
    @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysJob job) throws SchedulerException {
        SysJob newJob = jobService.selectJobById(job.getJobId());
        newJob.setStatus(job.getStatus());
        return toAjax(jobService.changeStatus(newJob));
    }

    @Operation(summary = "立即执行一次定时任务")
    @PreAuthorize("@ss.hasPermi('monitor:job:changeStatus')")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PutMapping("/run")
    public AjaxResult run(@RequestBody SysJob job) throws SchedulerException {
        boolean result = jobService.run(job);
        return result ? success() : error(MessageUtils.message("job.not.exist.or.expired"));
    }

    @Operation(summary = "删除定时任务")
    @PreAuthorize("@ss.hasPermi('monitor:job:remove')")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{jobIds}")
    public AjaxResult remove(@Parameter(description = "任务ID数组") @PathVariable Long[] jobIds) throws SchedulerException {
        jobService.deleteJobByIds(jobIds);
        return success();
    }
}
