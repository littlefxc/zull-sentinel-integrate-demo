//package com.fengxuechao.examples.zuul.sentinel;
//
//import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
//import com.alibaba.cloud.sentinel.zuul.SentinelZuulProperties;
//import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
//import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
//import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfig;
//import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
//import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
//import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
//import com.alibaba.csp.sentinel.cluster.server.config.ServerTransportConfig;
//import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
//import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
//import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
//import com.alibaba.csp.sentinel.transport.config.TransportConfig;
//import com.alibaba.csp.sentinel.util.HostNameUtil;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.TypeReference;
//import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
//import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
//import com.fengxuechao.examples.zuul.sentinel.entity.ClusterGroupEntity;
//import com.fengxuechao.examples.zuul.sentinel.filter.SentinelClusterParameterFlowZuulPreFilter;
//import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulErrorFilter;
//import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulPostFilter;
//import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import redis.clients.jedis.JedisCluster;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//import java.util.Optional;
//
//import static com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants.FILTER_ORDER_SENTINEL_CLUSTER_PARAMETER_FLOW;
//
///**
// * This pre-filter will regard all {@code proxyId} and all customized API as resources.
// * When a BlockException caught, the filter will try to find a fallback to execute.
// *
// * @author fengxuechao
// * @version 0.1
// * @date 2019/12/10
// */
//@Slf4j
//@Configuration
//@ConditionalOnProperty(prefix = CustomSentinelConstants.PREFIX_CLUSTER_PARAMETER_FLOW, name = "enabled", havingValue = "true")
//@EnableConfigurationProperties({CustomSentinelProperties.class, SentinelZuulProperties.class})
//public class SentinelClusterParameterFlowConfig1 implements InitializingBean {
//
//    @Autowired
//    @Qualifier("sentinel-json-param-flow-converter")
//    private JsonConverter jsonConverter;
//
//    @Autowired
//    private JedisCluster jedisCluster;
//
//    @Value("${spring.application.name}")
//    private String namespace;
//
//    @Bean
//    public SentinelClusterParameterFlowZuulPreFilter sentinelParameterFlowZuulPreFilter() {
//        return new SentinelClusterParameterFlowZuulPreFilter(FILTER_ORDER_SENTINEL_CLUSTER_PARAMETER_FLOW);
//    }
//
//    @Autowired
//    private SentinelZuulProperties zuulProperties;
//
//    @Bean
//    @ConditionalOnMissingBean
//    public SentinelZuulPostFilter sentinelZuulPostFilter() {
//        log.info("[Sentinel Zuul] register SentinelZuulPostFilter {}",
//                zuulProperties.getOrder().getPost());
//        return new SentinelZuulPostFilter(zuulProperties.getOrder().getPost());
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public SentinelZuulErrorFilter sentinelZuulErrorFilter() {
//        log.info("[Sentinel Zuul] register SentinelZuulErrorFilter {}",
//                zuulProperties.getOrder().getError());
//        return new SentinelZuulErrorFilter(zuulProperties.getOrder().getError());
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        log.info("本机地址 = {}", getCurrentMachineId());
//
//        initClusterUsingRedis();
//    }
//
//    /**
//     * 使用redis动态规则
//     */
//    private void initClusterUsingRedis() {
//        // 初始化单机热点参数限流规则
//        initDynamicRuleProperty();
//        // 集群限流 - 客户端通信配置
//        initClientConfigProperty();
//        // 集群限流 - 客户端分配配置
//        initClientServerAssignProperty();
//        // 集群限流 - 注册集群限流规则
//        registerClusterRuleSupplier();
//        // 注册集群限流服务端的传输配置
//        initServerTransportConfigProperty();
//        // 初始化集群限流服务端的状态
//        initStateProperty();
//    }
//
//    /**
//     * 初始化单机热点参数限流规则
//     */
//    @SuppressWarnings("unchecked")
//    private void initDynamicRuleProperty() {
//        ReadableDataSource<String, List<ParamFlowRule>> redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, namespace);
//        ParamFlowRuleManager.register2Property(redisDataSource.getProperty());
//    }
//
//    /**
//     * 集群限流 - 客户端通信配置
//     */
//    private void initClientConfigProperty() {
//        ClusterClientConfigManager.applyNewConfig(new ClusterClientConfig().setRequestTimeout(1000));
//    }
//
//    /**
//     * 集群限流 - 注册集群限流规则
//     */
//    @SuppressWarnings("unchecked")
//    private void registerClusterRuleSupplier() {
//        ClusterParamFlowRuleManager.setPropertySupplier(namespace -> new JedisPullDataSource<>(jsonConverter, jedisCluster, namespace).getProperty());
//    }
//
//    String source = "[{\"clientSet\":[\"192.168.120.81@8719\"],\"ip\":\"192.168.120.81\",\"machineId\":\"192.168.120.81@8720\",\"port\":18730}]";
//
//    /**
//     * 集群限流 - 客户端分配配置
//     */
//    private void initClientServerAssignProperty() {
//        // Cluster map format:
//        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
//        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
//        /*ReadableDataSource<String, ClusterClientAssignConfig> clientAssignDs = new NacosDataSource<>(remoteAddress, groupId,
//                clusterMapDataId, source -> {
//            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//            return Optional.ofNullable(groupList)
//                    .flatMap(this::extractClientAssignment)
//                    .orElse(null);
//        });
//        ClusterClientConfigManager.registerServerAssignProperty(clientAssignDs.getProperty());*/
//
//        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//        ClusterClientAssignConfig clusterClientAssignConfig = Optional.ofNullable(groupList)
//                .flatMap(this::extractClientAssignment)
//                .orElse(null);
//        ClusterClientConfigManager.applyNewAssignConfig(clusterClientAssignConfig);
//    }
//
//    /**
//     * 注册集群限流服务端的传输配置
//     */
//    private void initServerTransportConfigProperty() {
//        /*ReadableDataSource<String, ServerTransportConfig> serverTransportDs = new NacosDataSource<>(remoteAddress, groupId,
//                clusterMapDataId, source -> {
//            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//            return Optional.ofNullable(groupList)
//                    .flatMap(this::extractServerTransportConfig)
//                    .orElse(null);
//        });
//        ClusterServerConfigManager.registerServerTransportProperty(serverTransportDs.getProperty());*/
//
//        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//        ServerTransportConfig serverTransportConfig = Optional.ofNullable(groupList)
//                .flatMap(this::extractServerTransportConfig)
//                .orElse(null);
//        ClusterServerConfigManager.loadGlobalTransportConfig(serverTransportConfig);
//    }
//
//    /**
//     * 初始化集群限流服务端的状态
//     */
//    private void initStateProperty() {
//        // Cluster map format:
//        // [{"clientSet":["112.12.88.66@8729","112.12.88.67@8727"],"ip":"112.12.88.68","machineId":"112.12.88.68@8728","port":11111}]
//        // machineId: <ip@commandPort>, commandPort for port exposed to Sentinel dashboard (transport module)
//        /*ReadableDataSource<String, Integer> clusterModeDs = new NacosDataSource<>(remoteAddress, groupId,
//                clusterMapDataId, source -> {
//            List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//            return Optional.ofNullable(groupList)
//                    .map(this::extractMode)
//                    .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
//        });
//        ClusterStateManager.registerProperty(clusterModeDs.getProperty());*/
//
//        List<ClusterGroupEntity> groupList = JSON.parseObject(source, new TypeReference<List<ClusterGroupEntity>>() {});
//        Integer state = Optional.ofNullable(groupList)
//                .map(this::extractMode)
//                .orElse(ClusterStateManager.CLUSTER_NOT_STARTED);
//        ClusterStateManager.applyState(state);
//    }
//
//    private int extractMode(List<ClusterGroupEntity> groupList) {
//        // If any server group machineId matches current, then it's token server.
//        if (groupList.stream().anyMatch(this::machineEqual)) {
//            return ClusterStateManager.CLUSTER_SERVER;
//        }
//        // If current machine belongs to any of the token server group, then it's token client.
//        // Otherwise it's unassigned, should be set to NOT_STARTED.
//        boolean canBeClient = groupList.stream()
//                .flatMap(e -> e.getClientSet().stream())
//                .filter(Objects::nonNull)
//                .anyMatch(e -> e.equals(getCurrentMachineId()));
//        return canBeClient ? ClusterStateManager.CLUSTER_CLIENT : ClusterStateManager.CLUSTER_NOT_STARTED;
//    }
//
//    private Optional<ServerTransportConfig> extractServerTransportConfig(List<ClusterGroupEntity> groupList) {
//        return groupList.stream()
//                .filter(this::machineEqual)
//                .findAny()
//                .map(e -> new ServerTransportConfig().setPort(e.getPort()).setIdleSeconds(600));
//    }
//
//    private Optional<ClusterClientAssignConfig> extractClientAssignment(List<ClusterGroupEntity> groupList) {
//        if (groupList.stream().anyMatch(this::machineEqual)) {
//            return Optional.empty();
//        }
//        // Build client assign config from the client set of target server group.
//        for (ClusterGroupEntity group : groupList) {
//            if (group.getClientSet().contains(getCurrentMachineId())) {
//                String ip = group.getIp();
//                Integer port = group.getPort();
//                return Optional.of(new ClusterClientAssignConfig(ip, port));
//            }
//        }
//        return Optional.empty();
//    }
//
//    private boolean machineEqual(/*@Valid*/ ClusterGroupEntity group) {
//        return getCurrentMachineId().equals(group.getMachineId());
//    }
//
//    private String getCurrentMachineId() {
//        // Note: this may not work well for container-based env.
//        return HostNameUtil.getIp() + SEPARATOR + TransportConfig.getRuntimePort();
//    }
//
//    private static final String SEPARATOR = "@";
//}
