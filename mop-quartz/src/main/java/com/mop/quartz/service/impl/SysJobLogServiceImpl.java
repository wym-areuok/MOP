package com.mop.quartz.service.impl;

import com.mop.quartz.domain.SysJobLog;
import com.mop.quartz.mapper.SysJobLogMapper;
import com.mop.quartz.service.ISysJobLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 定时任务调度日志信息 服务层
 *
 * @author weiyiming
 */
@Service
public class SysJobLogServiceImpl implements ISysJobLogService {
    private static final Logger log = LoggerFactory.getLogger(SysJobLogServiceImpl.class);

    @Autowired
    private SysJobLogMapper jobLogMapper;

    /**
     * 获取quartz调度器日志的计划任务
     *
     * @param jobLog 调度日志信息
     * @return 调度任务日志集合
     */
    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    /**
     * 通过调度任务日志ID查询调度信息
     *
     * @param jobLogId 调度任务日志ID
     * @return 调度任务日志对象信息
     */
    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    /**
     * 新增任务日志
     *
     * @param jobLog 调度日志信息
     */
    @Override
    public void addJobLog(SysJobLog jobLog) {
        jobLogMapper.insertJobLog(jobLog);
    }

    /**
     * 批量删除调度日志信息
     *
     * @param logIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteJobLogByIds(Long[] logIds) {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    /**
     * 删除任务日志
     *
     * @param jobId 调度日志ID
     */
    @Override
    public int deleteJobLogById(Long jobId) {
        return jobLogMapper.deleteJobLogById(jobId);
    }

    /**
     * 清空任务日志
     */
    @Override
    public void cleanJobLog() {
        jobLogMapper.cleanJobLog();
    }

    /**
     * 清理过期任务日志（由 Quartz 定时任务调用，每天凌晨3点，删除90天前的日志）
     * <p>
     * 注意：此方法已从 {@code @Scheduled} 迁移至 Quartz 管理（{@code sys_job} 表 job_id=4），
     * 集群环境下 Quartz JDBC JobStore 保证仅一个节点执行。
     * </p>
     *
     * @param days 保留天数
     * @return 删除的记录数
     */
    @Override
    public int cleanExpiredJobLog(int days) {
        try {
            int deleted = jobLogMapper.cleanExpiredJobLog(days);
            if (deleted > 0) {
                log.info("清理过期定时任务日志完成，共删除 {} 条（{}天前）", deleted, days);
            }
            return deleted;
        } catch (Exception e) {
            log.error("清理过期定时任务日志失败", e);
            throw e;
        }
    }
}
