package com.mop.quartz.util;

import com.mop.common.constant.Constants;
import com.mop.common.constant.ScheduleConstants;
import com.mop.common.utils.ExceptionUtil;
import com.mop.common.utils.StringUtils;
import com.mop.common.utils.bean.BeanUtils;
import com.mop.common.utils.spring.SpringUtils;
import com.mop.quartz.domain.SysJob;
import com.mop.quartz.domain.SysJobLog;
import com.mop.quartz.service.ISysJobLogService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.*;

/**
 * 抽象quartz调用
 *
 * <h3>执行流程</h3>
 * <pre>{@code
 * execute(context)
 *   ├── before(context, sysJob)           // 记录开始时间
 *   ├── defaultJobTimeoutMinutes > 0 ?
 *   │     ├── 是 → doExecuteWithTimeout() // 带超时控制（见下方）
 *   │     └── 否 → doExecute(context, sysJob)  // 原始逻辑
 *   └── after(context, sysJob, exception) // 记录执行日志（成功/失败/超时）
 * }</pre>
 *
 * <h3>超时控制原理</h3>
 * <pre>{@code
 * doExecuteWithTimeout(context, sysJob)
 *   │
 *   │  将 doExecute() 提交到守护线程池 TIMEOUT_EXECUTOR
 *   ▼
 *   future = executor.submit(() -> doExecute(context, sysJob))
 *   │
 *   │  阻塞等待，最多等 defaultJobTimeoutMinutes 分钟
 *   ▼
 *   future.get(timeout, TimeUnit.MINUTES)
 *   │
 *   ├── 正常完成 → 返回
 *   │
 *   └── TimeoutException → future.cancel(true) 中断工作线程
 *        ↓
 *        抛 RuntimeException("任务执行超时: xxx (限制: N分钟)")
 *        ↓
 *        被外层 catch(Exception) 捕获
 *        ↓
 *        after() 记录 FAIL 状态 + 超时异常信息到调度日志
 *        ↓
 *        Quartz 工作线程释放，下一个任务可正常调度
 * }</pre>
 *
 * <h3>关键设计</h3>
 * <ul>
 *   <li><b>守护线程池</b>：超时工作线程为 daemon 线程，不会阻止 JVM 正常退出。</li>
 *   <li><b>异常还原</b>：{@code ExecutionException} 中包装的原始 checked exception
 *       会被提取并原样抛出，确保 {@code after()} 收到准确的异常类型。</li>
 *   <li><b>向后兼容</b>：{@code defaultJobTimeoutMinutes = 0} 时完全走原始逻辑，
 *       无任何线程池开销。</li>
 *   <li><b>全局统一</b>：所有任务共享同一超时配置，由
 *       {@link com.mop.quartz.config.QuartzTimeoutConfig} 在启动时注入。</li>
 * </ul>
 *
 * @author weiyiming
 */
public abstract class AbstractQuartzJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);
    /**
     * 用于超时控制的任务执行线程池（守护线程，不阻止 JVM 退出）
     */
    private static final ExecutorService TIMEOUT_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "quartz-timeout-worker");
        t.setDaemon(true);
        return t;
    });
    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();
    /**
     * 全局任务执行超时时间（分钟），0 表示不限制。
     * 由 {@link com.mop.quartz.config.QuartzTimeoutConfig} 在启动时注入。
     */
    private static volatile long defaultJobTimeoutMinutes = 0;

    /**
     * 设置全局任务超时时间（由配置类调用）
     *
     * @param minutes 超时分钟数，0=不限制
     */
    public static void setDefaultJobTimeoutMinutes(long minutes) {
        defaultJobTimeoutMinutes = minutes;
    }

    @Override
    public void execute(JobExecutionContext context) {
        SysJob sysJob = new SysJob();
        BeanUtils.copyBeanProp(sysJob, context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES));
        try {
            before(context, sysJob);
            if (defaultJobTimeoutMinutes > 0) {
                doExecuteWithTimeout(context, sysJob);
            } else {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("任务执行异常 - jobName: {}, invokeTarget: {}", sysJob.getJobName(), sysJob.getInvokeTarget(), e);
            try {
                after(context, sysJob, e);
            } catch (Exception logEx) {
                log.error("任务日志记录失败 - jobName: {}", sysJob.getJobName(), logEx);
            }
        }
    }

    /**
     * 带超时控制的任务执行
     * <p>
     * 将实际任务提交到独立守护线程池，通过 {@link Future#get(long, TimeUnit)} 限制
     * 最大执行时间。超时后中断工作线程并抛出异常，释放 Quartz 工作线程。
     * </p>
     * <p>
     * <b>注意</b>：{@code JobExecutionContext} 在 Quartz 中与执行线程绑定，
     * 跨线程传递后不应再调用其生命周期相关方法（如 {@code getNextFireTime()} 等）。
     * 本项目所有子类的 {@code doExecute()} 实现仅使用 {@code sysJob} 参数，
     * 不依赖 {@code context}，因此安全。
     * </p>
     */
    private void doExecuteWithTimeout(JobExecutionContext context, SysJob sysJob) throws Exception {
        Future<?> future = TIMEOUT_EXECUTOR.submit(() -> {
            try {
                doExecute(context, sysJob);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            future.get(defaultJobTimeoutMinutes, TimeUnit.MINUTES);
        } catch (TimeoutException e) {
            // 超时：取消任务并中断工作线程
            boolean cancelled = future.cancel(true);
            log.error("任务执行超时 - jobName: {}, 超时限制: {}分钟, 已中断: {}",
                    sysJob.getJobName(), defaultJobTimeoutMinutes, cancelled);
            throw new RuntimeException(
                    "任务执行超时: " + sysJob.getJobName() + " (限制: " + defaultJobTimeoutMinutes + "分钟)", e);
        } catch (ExecutionException e) {
            // 还原原始异常
            Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException(cause);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     */
    protected void before(JobExecutionContext context, SysJob sysJob) {
        threadLocal.set(new Date());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     */
    protected void after(JobExecutionContext context, SysJob sysJob, Exception e) {
        Date startTime = threadLocal.get();
        threadLocal.remove();

        final SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(sysJob.getJobName());
        sysJobLog.setJobGroup(sysJob.getJobGroup());
        sysJobLog.setInvokeTarget(sysJob.getInvokeTarget());
        sysJobLog.setStartTime(startTime);
        sysJobLog.setEndTime(new Date());
        long runMs = sysJobLog.getEndTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + " 总共耗时：" + runMs + "毫秒");
        if (e != null) {
            sysJobLog.setStatus(Constants.FAIL);
            String errorMsg = StringUtils.substring(ExceptionUtil.getExceptionMessage(e), 0, 2000);
            sysJobLog.setExceptionInfo(errorMsg);
        } else {
            sysJobLog.setStatus(Constants.SUCCESS);
        }

        // 写入数据库当中
        SpringUtils.getBean(ISysJobLogService.class).addJobLog(sysJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob  系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;
}
