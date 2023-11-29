package com.video.dao.sqlprovider;

import org.apache.ibatis.jdbc.SQL;

/**
 * @author tian
 * @since 2023/11/19
 */
public class CommonSqlProvider {

    private CommonSqlProvider() {}

    public static String updateAuthById(String table) {
        return new SQL() {{
            UPDATE(table);
            SET("is_uploadable=#{isUploadable}");
            SET("is_deletable=#{isDeletable}");
            SET("is_updatable=#{isUpdatable}");
            SET("is_visible=#{isVisible}");
            WHERE("id=#{id}");
        }}.toString();
    }
}
