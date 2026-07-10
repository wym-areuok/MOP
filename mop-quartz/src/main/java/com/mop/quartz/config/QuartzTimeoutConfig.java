package com.mop.quartz.config;

import com.mop.quartz.util.AbstractQuartzJob;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 任务执行超时配置
 *
 * <h3>背景</h3>
 * 项目中所有定时任务通过 {@link AbstractQuartzJob} 执行，该抽象类直接实现
 * Quartz 的 {@code Job} 接口，未使用 {@code InterruptableJob}，也没有
 * 执行超时控制。当任务因死循环、外部接口长时间无响应等原因卡住时，会永久
 * 占用 Quartz 工作线程，最终耗尽线程池导致所有定时任务停止调度。
 *
 * <h3>原理</h3>
 * <ol>
 *   <li>读取配置项 {@code quartz.jobTimeoutMinutes}（默认 0 = 不限制）。</li>
 *   <li>应用启动时通过 {@link #init()} 将超时值注入
 *       {@link AbstractQuartzJob#setDefaultJobTimeoutMinutes(long)}。</li>
 *   <li>{@code AbstractQuartzJob} 在执行时将 {@code doExecute()}
 *       提交到一个独立的守护线程池，通过 {@code Future.get(timeout)} 限制
 *       最大执行时间。</li>
 *   <li>超时后自动中断工作线程并记录失败日志，释放 Quartz 工作线程。</li>
 * </ol>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * quartz:
 *   jobTimeoutMinutes: 30   # 单任务最长执行 30 分钟
 * }</pre>
 * 设为 0 则关闭超时控制，保持原有行为。
 *
 * @author weiyiming
 * @see AbstractQuartzJob
 */
@Configuration
public class QuartzTimeoutConfig {

    private static final Logger log = LoggerFactory.getLogger(QuartzTimeoutConfig.class);

    @Value("${quartz.jobTimeoutMinutes:0}")
    private long jobTimeoutMinutes;

    @PostConstruct
    public void init() {
        AbstractQuartzJob.setDefaultJobTimeoutMinutes(jobTimeoutMinutes);
        if (jobTimeoutMinutes > 0) {
            log.info("Quartz 任务执行超时已设置: {} 分钟", jobTimeoutMinutes);
        } else {
            log.info("Quartz 任务执行超时未启用 (jobTimeoutMinutes=0)");
        }
    }
}
