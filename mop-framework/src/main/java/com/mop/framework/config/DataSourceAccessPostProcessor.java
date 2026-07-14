package com.mop.framework.config;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.mop.framework.datasource.AccessControlledDataSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * 在 DynamicRoutingDataSource 初始化完成后，自动包装一层访问控制
 * <p>
 * 包装后的 AccessControlledDataSource 会在每次 getConnection() 时
 * 检查字典开关，确保被停用的外部数据源无法被访问。
 *
 * @author weiyiming
 */
@Component
public class DataSourceAccessPostProcessor implements BeanPostProcessor {

    @Autowired
    private DynamicDsFilter dynamicDsFilter;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DynamicRoutingDataSource) {
            return new AccessControlledDataSource((DynamicRoutingDataSource) bean, dynamicDsFilter);
        }
        return bean;
    }
}
