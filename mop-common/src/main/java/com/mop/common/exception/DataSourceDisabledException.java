package com.mop.common.exception;

/**
 * 数据源被停用异常
 * <p>
 * 当尝试访问字典中 dictValue='N' 的外部数据源时抛出。
 *
 * @author weiyiming
 */
public class DataSourceDisabledException extends RuntimeException {

    public DataSourceDisabledException(String message) {
        super(message);
    }
}
