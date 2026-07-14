package com.mop.framework.config;

import com.mop.common.core.domain.entity.SysDictData;
import com.mop.system.mapper.SysDictDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据源字典开关过滤器
 * <p>
 * 启动时从字典表 datasource_switch 加载开关配置，
 * 运行时可通过 DatasourceReloadController 调用 refresh() 刷新。
 * <p>
 * 字典规范：
 * - dictType: datasource_switch
 * - dictLabel: 数据源名称（如 server_a）
 * - dictValue: Y（启用）/ N（停用）
 *
 * @author weiyiming
 */
@Component
public class DynamicDsFilter implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DynamicDsFilter.class);
    private static final String DICT_TYPE = "datasource_switch";

    @Autowired
    private SysDictDataMapper dictDataMapper;

    /**
     * 当前启用的外部数据源集合（线程安全）
     */
    private volatile Set<String> enabledDataSources = Collections.emptySet();

    @Override
    public void run(ApplicationArguments args) {
        refresh();
    }

    /**
     * 重新加载字典配置（绕过缓存，直查数据库）
     */
    public synchronized void refresh() {
        List<SysDictData> list = dictDataMapper.selectDictDataByType(DICT_TYPE);
        if (list != null) {
            enabledDataSources = list.stream()
                    .filter(d -> "0".equals(d.getStatus()))       // 字典状态正常
                    .filter(d -> "Y".equals(d.getDictValue()))    // dictValue=Y 表示启用
                    .map(SysDictData::getDictLabel)               // dictLabel = 数据源名称
                    .collect(Collectors.toSet());
        }
        log.info("数据源开关已刷新, 启用的外部数据源: {}", enabledDataSources);
    }

    /**
     * 判断指定数据源是否允许访问
     * <p>
     * master（主数据源）永远允许访问，不受字典控制。
     *
     * @param dsName 数据源名称
     * @return true 允许访问
     */
    public boolean isEnabled(String dsName) {
        return "master".equals(dsName) || enabledDataSources.contains(dsName);
    }
}
