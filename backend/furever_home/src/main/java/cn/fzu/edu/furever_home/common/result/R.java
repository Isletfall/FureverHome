package cn.fzu.edu.furever_home.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用结果返回类
 * @param <T> 数据类型
 */
@Data
public class R<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 成功
     */
    public static <T> R<T> ok() {
        return restResult(null, 200, "操作成功");
    }
    
    /**
     * 成功
     */
    public static <T> R<T> ok(T data) {
        return restResult(data, 200, "操作成功");
    }
    
    /**
     * 成功
     */
    public static <T> R<T> ok(T data, String message) {
        return restResult(data, 200, message);
    }
    
    /**
     * 失败
     */
    public static <T> R<T> fail() {
        return restResult(null, 500, "操作失败");
    }
    
    /**
     * 失败
     */
    public static <T> R<T> fail(String message) {
        return restResult(null, 500, message);
    }
    
    /**
     * 失败
     */
    public static <T> R<T> fail(Integer code, String message) {
        return restResult(null, code, message);
    }
    
    /**
     * 构建结果
     */
    private static <T> R<T> restResult(T data, Integer code, String message) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setData(data);
        r.setMessage(message);
        return r;
    }
} 