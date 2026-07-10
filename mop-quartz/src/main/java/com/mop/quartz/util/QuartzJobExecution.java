package com.mop.quartz.util;

import com.mop.quartz.domain.SysJob;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（允许并发执行）
 * <p>
 * 与 {@link QuartzDisallowConcurrentExecution} 对应，该类不添加
 * {@code @DisallowConcurrentExecution} 注解，表示同一任务可在多个
 * 线程中同时执行。适用于幂等操作或无状态的任务。
 * </p>
 *
 * @author weiyiming
 */
public class QuartzJobExecution extends AbstractQuartzJob {
    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
