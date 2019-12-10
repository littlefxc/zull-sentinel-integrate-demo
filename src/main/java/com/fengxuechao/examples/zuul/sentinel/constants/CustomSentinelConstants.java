package com.fengxuechao.examples.zuul.sentinel.constants;

/**
 * 自定义配置使用 Sentinel 的常量
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/28
 */
public interface CustomSentinelConstants {

    String PREFIX = "custom.sentinel";

    String PREFIX_GATEWAY_FLOW = PREFIX + ".gateway-flow";

    String PREFIX_PARAMETER_FLOW = PREFIX + ".parameter-flow";

    String PREFIX_CLUSTER_PARAMETER_FLOW = PREFIX + ".cluster-parameter-flow";

    /**
     * 集群限流-热点参数过滤器的默认优先级
     */
    int FILTER_ORDER_SENTINEL_CLUSTER_PARAMETER_FLOW = 9998;

    /**
     * 热点参数限流过滤器的默认优先级
     */
    int FILTER_ORDER_SENTINEL_PARAMETER_FLOW = 9999;
}
