package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.BlockResponse;

/**
 * 自定义返回限流返回对象
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/20
 */
public class CustomBlockResponse extends BlockResponse {

    public CustomBlockResponse(int code, String message, String route) {
        super(code, message, route);
    }

    /**
     * 必须重写，才能够自定义限流返回消息
     *
     * @return
     */
    @Override
    public String toString() {
        return "{\"errorCode\":\"" + this.getCode() + "\", \"errorMsg\":\"" + this.getMessage() + "\"}";
    }
}
