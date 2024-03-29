package com.video.enums;

/**
 * 拦截级别
 *
 * @author tian
 * @since 2023/11/25
 */
public enum InterceptorLevel {
    /**
     * 不拦截
     */
    NONE,

    /**
     * 用户级别拦截
     */
    USER,

    /**
     * 管理员级别拦截
     */
    ADMIN,

    /**
     * 系统用户
     */
    SYSTEM
}
