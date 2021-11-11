package com.moduo.minio.model.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Wu Zicong
 * @create 2021-11-10 10:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RespBean<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private String message;
    private T obj;
    public static <T>RespBean<T> info(Integer code, String message, T obj){
        return new RespBean(code,message, obj == null ? "暂无承载数据" : obj);
    }
    public static <T>RespBean<T> info(Integer code,String message){
        return new RespBean(code,message,null);
    }
    public static <T>RespBean<T> success(String message){
        return new RespBean(200,message,null);
    }
}
