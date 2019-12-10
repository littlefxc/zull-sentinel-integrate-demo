package com.fengxuechao.examples.zuul.sentinel.properties;

import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * 自定义使用 Sentinel 使用的配置属性
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/28
 */
@Data
@ConfigurationProperties(CustomSentinelConstants.PREFIX)
public class CustomSentinelProperties {

    /**
     * 开启网关流控
     */
    private Boolean enableGatewayFlow = false;

    /**
     * 缓存规则key
     */
    private String gwFlowKey = "dev:rule_key";

    /**
     * TODO 目前 jedisCluster 会阻塞程序的启动
     */
    private String gwFlowChanel = "dev:chanel";
}
