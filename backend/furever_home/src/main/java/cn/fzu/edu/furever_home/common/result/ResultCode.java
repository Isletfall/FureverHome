package cn.fzu.edu.furever_home.common.result;

import lombok.Getter;

/**
 * 返回结果状态码枚举
 */
@Getter
public enum ResultCode {
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    ERROR(500, "操作失败"),

    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证或认证失效，请重新登录"),

    /**
     * 未授权
     */
    FORBIDDEN(403, "无操作权限"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 消息
     */
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
} 