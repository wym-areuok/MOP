package com.mop.framework.datasource;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.mop.common.exception.DataSourceDisabledException;
import com.mop.framework.config.DynamicDsFilter;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 数据源访问控制包装器
 * <p>
 * 在 DynamicRoutingDataSource 外层拦截所有 getConnection() 调用，
 * 根据字典开关决定是否放行。拦截点位于 DataSource 层，覆盖
 * MyBatis / JdbcTemplate / 原生 JDBC 所有访问路径。
 * <p>
 * 双层 AND 控制：
 * yml 有连接信息（数据源被注册） AND 字典中启用（本包装器不抛异常）
 *
 * @author weiyiming
 */
public class AccessControlledDataSource implements DataSource {

    private final DynamicRoutingDataSource delegate;
    private final DynamicDsFilter filter;

    public AccessControlledDataSource(DynamicRoutingDataSource delegate, DynamicDsFilter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Connection getConnection() throws SQLException {
        checkAccess();
        return delegate.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        checkAccess();
        return delegate.getConnection(username, password);
    }

    /**
     * 检查当前线程要访问的数据源是否在字典中被启用
     */
    private void checkAccess() {
        String dsKey = DynamicDataSourceContextHolder.peek();
        if (dsKey == null || "master".equals(dsKey)) {
            return;
        }
        if (!filter.isEnabled(dsKey)) {
            throw new DataSourceDisabledException(
                    "数据源 [" + dsKey + "] 已被管理员停用，请联系管理员启用后再试");
        }
    }

    // ==================== 以下全部委托给底层 DataSource ====================

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || delegate.isWrapperFor(iface);
    }
}
