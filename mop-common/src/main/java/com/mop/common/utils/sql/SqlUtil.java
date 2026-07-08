package com.mop.common.utils.sql;

import com.mop.common.exception.UtilException;
import com.mop.common.utils.MessageUtils;
import com.mop.common.utils.StringUtils;

/**
 * sql操作工具类
 *
 * @author weiyiming
 */
public class SqlUtil {
    /**
     * 限制orderBy最大长度
     */
    private static final int ORDER_BY_MAX_LENGTH = 500;
    /**
     * 定义常用的 sql关键字
     */
    public static String SQL_REGEX = "\u000B|%0A|and |extractvalue|updatexml|sleep|waitfor|information_schema|sys\\.|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";
    /**
     * 仅支持字母、数字、下划线、空格、逗号、小数点（支持多个字段排序）
     */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";

    /**
     * 检查字符，防止注入绕过
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new UtilException(MessageUtils.message("sql.param.invalid"));
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH) {
            throw new UtilException(MessageUtils.message("sql.param.exceed.max"));
        }
        return value;
    }

    /**
     * 验证 order by 语法是否符合规范
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * SQL关键字检查
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String normalizedValue = value.replaceAll("\\p{Z}|\\s", "");
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(normalizedValue, sqlKeyword) > -1) {
                throw new UtilException(MessageUtils.message("sql.keyword.risk", sqlKeyword));
            }
        }
    }
}
