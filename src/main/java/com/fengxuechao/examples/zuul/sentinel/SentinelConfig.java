package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.zuul.handler.FallBackProviderHandler;
import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.config.ClusterServerConfigManager;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * 流量控制配置类
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/15
 * @see <a href="https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E9%99%90%E6%B5%81">网关限流</a>
 * @see <a href="https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95">动态规则扩展</a>
 * @see <a href="https://github.com/alibaba/Sentinel/wiki/%E9%9B%86%E7%BE%A4%E6%B5%81%E6%8E%A7">集群流控</a>
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({CustomSentinelProperties.class})
public class SentinelConfig implements InitializingBean {

    @Autowired
    @Qualifier("sentinel-json-gw-flow-converter")
    private JsonConverter jsonConverter;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CustomSentinelProperties customSentinelProperties;

    /**
     * 自定义限流返回消息
     * 与业务的返回消息保持一致
     *
     * @return CustomZuulBlockFallbackProvider
     * @see CustomBlockResponse CustomBlockResponse 自定义 BlockResponse,重写 toString() 自定义返回消息
     * @see FallBackProviderHandler FallBackProviderHandler 实现了接口 SmartInitializingSingleton，故此利用 Spring Bean 生命周期原理将默认的 ZuulBlockFallbackProvider 替换为自定义的返回限流处理
     * @see SentinelZuulPreFilter SentinelZuulPreFilter 捕获 BlockException, 设置限流返回消息，也就是 CustomBlockResponse
     */
    @Bean
    public CustomZuulBlockFallbackProvider customZuulBlockFallbackProvider() {
        return new CustomZuulBlockFallbackProvider();
    }

    /**
     * bean 创建完成后执行
     * 1. 注册sentinel网关流控
     * 2. 注册sentinel集群流控
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String gwFlowKey = customSentinelProperties.getGwFlowKey();
        String gwFlowChanel = customSentinelProperties.getGwFlowChanel();
        JedisPullDataSource redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, gwFlowKey, gwFlowChanel);
        // 网关流控无法做到集群流控的功能，不适配我们现有的业务，应当需自定义
        GatewayRuleManager.register2Property(redisDataSource.getProperty());
    }

    /* 下面代码部分是测试时遗留的代码， */

    /**
     * 手动修改规则（硬编码方式）一般仅用于测试和演示，生产上一般通过动态规则源的方式来动态管理规则。
     * 由于业务上定义接口是由 method 和 v 来决定的，故此这部分不需要
     */
    @Deprecated
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition api1 = new ApiDefinition()
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/rest").setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});
        definitions.add(api1);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    @Deprecated
//    @PostConstruct
    public void doInit() {
        // 手动修改规则（硬编码方式）一般仅用于测试和演示，生产上一般通过动态规则源的方式来动态管理规则。
//        initGatewayRules();
    }

    /**
     * 这部分需要配置动态生效
     */
    @Deprecated
    private void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("capacity-group-protocol-adaptor-consumer")
                        // 限流阈值
                        .setCount(1)
                        // 统计时间窗口，单位是秒，默认是 1 秒。
                        .setIntervalSec(20)
                        // 流量整形的控制效果，同限流规则的 controlBehavior 字段，目前支持快速失败和匀速排队两种模式，默认是快速失败。
//                .setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT)
                        // 应对突发请求时额外允许的请求数目
//            .setBurst(1)
                        // 匀速排队模式下的最长排队时间，单位是毫秒，仅在匀速排队模式下生效。
//            .setMaxQueueingTimeoutMs(2)
                        // 参数限流配置。若不提供，则代表不针对参数进行限流，该网关规则将会被转换成普通流控规则；否则会转换成热点规则。
                        .setParamItem(new GatewayParamFlowItem()
                                // 从请求中提取参数的策略,目前支持提取来源
                                // IP（PARAM_PARSE_STRATEGY_CLIENT_IP）、
                                // Host（PARAM_PARSE_STRATEGY_HOST）、
                                // 任意 Header（PARAM_PARSE_STRATEGY_HEADER）和
                                // 任意 URL 参数（PARAM_PARSE_STRATEGY_URL_PARAM）四种模式。
                                .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_URL_PARAM)
                                // 参数值的匹配策略，目前支持
                                // 精确匹配（PARAM_MATCH_STRATEGY_EXACT）、
                                // 子串匹配（PARAM_MATCH_STRATEGY_CONTAINS）和
                                // 正则匹配（PARAM_MATCH_STRATEGY_REGEX）。（1.6.2 版本开始支持）
                                .setMatchStrategy(SentinelGatewayConstants.PARAM_MATCH_STRATEGY_EXACT)
                                // 若提取策略选择 Header 模式或 URL 参数模式，则需要指定对应的 header 名称或 URL 参数名称。
                                .setFieldName("accessToken")
                                // 参数值的匹配模式，只有匹配该模式的请求属性值会纳入统计和流控；若为空则统计该请求属性的所有值。（1.6.2 版本开始支持）
                                .setPattern("325144132CC3BC7C433CD64C0BE98CC8")
                        )
        );
        GatewayRuleManager.loadRules(rules);
    }
}
