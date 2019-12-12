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

    private Boolean enabled = false;

    /**
     * 网关流控配置
     */
    private GatewayFlowProperties gatewayFlow = new GatewayFlowProperties();

    /**
     * 热点参数限流配置
     */
    private ParameterFlowProperties parameterFlow = new ParameterFlowProperties();

    /**
     * 集群限流 - 客户端/服务端配置
     */
    private ClusterProperties cluster = new ClusterProperties();

    /**
     * 集群限流 - 热点参数限流配置
     */
    private ClusterParameterFlowProperties clusterParameterFlow = new ClusterParameterFlowProperties();

    @ConfigurationProperties(CustomSentinelConstants.PREFIX_GATEWAY_FLOW)
    public GatewayFlowProperties getGatewayFlow() {
        return gatewayFlow;
    }

    @ConfigurationProperties(CustomSentinelConstants.PREFIX_PARAMETER_FLOW)
    public ParameterFlowProperties getParameterFlow() {
        return parameterFlow;
    }

    @ConfigurationProperties(CustomSentinelConstants.PREFIX_CLUSTER_PARAMETER_FLOW)
    public ClusterParameterFlowProperties getClusterParameterFlow() {
        return clusterParameterFlow;
    }

    @ConfigurationProperties(CustomSentinelConstants.PREFIX_CLUSTER_CLIENT)
    public ClusterProperties getCluster() {
        return cluster;
    }
}
