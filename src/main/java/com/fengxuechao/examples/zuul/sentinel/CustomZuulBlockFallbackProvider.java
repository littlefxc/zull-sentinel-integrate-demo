package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * 自定义限流的 fallback
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/20
 */
public class CustomZuulBlockFallbackProvider implements ZuulBlockFallbackProvider {
    /**
     * The route this fallback will be used for.
     *
     * @return The route the fallback will be used for.
     */
    @Override
    public String getRoute() {
        return "*";
    }

    /**
     * Provides a fallback response based on the cause of the failed execution.
     *
     * @param route The route the fallback is for
     * @param cause cause of the main method failure, may be <code>null</code>
     * @return the fallback response
     */
    @Override
    public CustomBlockResponse fallbackResponse(String route, Throwable cause) {
        if (cause instanceof BlockException) {
            return new CustomBlockResponse(429, "Sentinel block exception", route);
        } else {
            return new CustomBlockResponse(500, "System Error", route);
        }
    }
}
