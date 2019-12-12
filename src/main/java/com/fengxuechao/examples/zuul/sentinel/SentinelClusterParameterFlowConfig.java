package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.zuul.SentinelZuulProperties;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelParameterFlowZuulPreFilter;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulErrorFilter;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulPostFilter;
import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import redis.clients.jedis.JedisCluster;

import java.util.List;

import static com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants.FILTER_ORDER_SENTINEL_CLUSTER_PARAMETER_FLOW;

/**
 * This pre-filter will regard all {@code proxyId} and all customized API as resources.
 * When a BlockException caught, the filter will try to find a fallback to execute.
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = CustomSentinelConstants.PREFIX_CLUSTER_PARAMETER_FLOW, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({CustomSentinelProperties.class, SentinelZuulProperties.class})
public class SentinelClusterParameterFlowConfig implements InitializingBean {

    @Autowired
    @Qualifier("sentinel-json-param-flow-converter")
    private JsonConverter jsonConverter;

    @Autowired
    private JedisCluster jedisCluster;

    @Value("${spring.application.name}")
    private String namespace;

    @Bean
    public SentinelParameterFlowZuulPreFilter sentinelParameterFlowZuulPreFilter() {
        log.info("[Sentinel Zuul] register SentinelZuulPreFilter {}",
                zuulProperties.getOrder().getPre());
        return new SentinelParameterFlowZuulPreFilter(zuulProperties.getOrder().getPre());
    }

    @Autowired
    private SentinelZuulProperties zuulProperties;

    @Bean
    @ConditionalOnMissingBean
    public SentinelZuulPostFilter sentinelZuulPostFilter() {
        log.info("[Sentinel Zuul] register SentinelZuulPostFilter {}",
                zuulProperties.getOrder().getPost());
        return new SentinelZuulPostFilter(zuulProperties.getOrder().getPost());
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelZuulErrorFilter sentinelZuulErrorFilter() {
        log.info("[Sentinel Zuul] register SentinelZuulErrorFilter {}",
                zuulProperties.getOrder().getError());
        return new SentinelZuulErrorFilter(zuulProperties.getOrder().getError());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initClusterUsingRedis();
    }

    /**
     * 使用redis动态规则
     */
    private void initClusterUsingRedis() {
        // 初始化单机热点参数限流规则
        initDynamicRuleProperty();
        // 集群限流 - 客户端通信配置
        initClientConfigProperty();
        // 集群限流 - 客户端分配配置
        initClientServerAssignProperty();
        // 集群限流 - 注册集群限流规则
        registerClusterRuleSupplier();
        // 注册集群限流服务端的传输配置
        initServerTransportConfigProperty();
        // 初始化集群限流服务端的状态
        initStateProperty();
    }

    /**
     * 初始化单机热点参数限流规则
     */
    @SuppressWarnings("unchecked")
    private void initDynamicRuleProperty() {
        ReadableDataSource<String, List<ParamFlowRule>> redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, namespace);
        ParamFlowRuleManager.register2Property(redisDataSource.getProperty());
    }

    /**
     * 集群限流 - 客户端通信配置
     */
    private void initClientConfigProperty() {
        ClusterClientConfigManager.applyNewConfig(new ClusterClientConfig().setRequestTimeout(1000));
    }

    /**
     * 集群限流 - 注册集群限流规则
     */
    @SuppressWarnings("unchecked")
    private void registerClusterRuleSupplier() {
        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> new JedisPullDataSource<>(jsonConverter, jedisCluster, namespace).getProperty());
    }

    @Value("${custom.sentinel.token-server-ip}")
    private String tokenServerIp;

    @Value("${custom.sentinel.token-server-port}")
    private Integer tokenServerPort;

    @Value("${custom.sentinel.token-client-port}")
    private Integer tokenClientPort;

    /**
     * 集群限流 - 客户端分配配置
     */
    private void initClientServerAssignProperty() {
        ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig(tokenServerIp, tokenServerPort);
        ClusterClientConfigManager.applyNewAssignConfig(clusterClientAssignConfig);
    }

    /**
     * 注册集群限流服务端的传输配置
     */
    private void initServerTransportConfigProperty() {
        //指定提供TokenService的端口和地址
        ServerTransportConfig serverTransportConfig = new ServerTransportConfig(tokenClientPort, 600);
        //加载配置
        ClusterServerConfigManager.loadGlobalTransportConfig(serverTransportConfig);
    }

    @Autowired
    Environment environment;

    /**
     * 初始化集群限流服务端的状态
     */
    private void initStateProperty() {
        if (tokenServerPort.equals(tokenClientPort)) {
            ClusterStateManager.applyState(ClusterStateManager.CLUSTER_SERVER);
        } else {
            ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);
        }
        log.debug("该限流客户端状态, mode = {}", ClusterStateManager.getMode());
    }
}
