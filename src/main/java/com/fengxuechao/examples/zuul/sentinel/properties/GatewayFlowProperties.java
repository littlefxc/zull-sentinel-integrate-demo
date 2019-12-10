package com.fengxuechao.examples.zuul.sentinel.properties;

import lombok.Data;

/**
 * 网关流控配置
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Data
public class GatewayFlowProperties {

    /**
     * 开启网关流控
     */
    private Boolean enabled = false;

    /**
     * 网关流控缓存 Key
     */
    private String key = "dev:gateway_flow";

    /**
     * TODO 目前 jedisCluster 的 pub/sub 会阻塞程序的启动
     */
    private String chanel = "dev:gateway_flow_chanel";
}
