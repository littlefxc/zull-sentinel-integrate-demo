package com.fengxuechao.examples.zuul.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.split;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/7/1
 */
@ConditionalOnProperty(value = "redis.cluster.enabled", havingValue = "true")
@EnableConfigurationProperties(RedisClusterProperties.class)
@Configuration
public class RedisConfig {

    @Autowired
    private RedisClusterProperties properties;

    /**
     * jedis cluster
     *
     * @return
     */
    @Bean
    public JedisCluster jedisCluster() {
        String[] hostAndPorts = properties.getNodes().split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        for (String node : hostAndPorts) {
            nodes.add(readHostAndPortFromString(node));
        }
        // 创建集群对象
        if (StringUtils.isNotBlank(properties.getPassword())) {
            return new JedisCluster(nodes, properties.getTimeout(), properties.getTimeout(),
                properties.getMaxAttempts(), properties.getPassword(), jedisPoolConfig());
        } else {
            return new JedisCluster(nodes, properties.getTimeout(), properties.getTimeout(),
                properties.getMaxAttempts(), jedisPoolConfig());
        }
    }

    /**
     * 读取集群字符串创建HostAndPort
     *
     * @param hostAndPort
     * @return
     */
    private HostAndPort readHostAndPortFromString(String hostAndPort) {

        String[] args = split(hostAndPort, ":");

        notNull(args, "HostAndPort need to be seperated by  ':'.");
        isTrue(args.length == 2, "Host and Port String needs to specified as host:port");
        return new HostAndPort(args[0], Integer.parseInt(args[1]));
    }

    /**
     * jedis 线程池配置
     *
     * @return
     */
    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(properties.getMaxIdle());
        jedisPoolConfig.setMinIdle(properties.getMinIdle());
        jedisPoolConfig.setMaxTotal(properties.getMaxActive());
        jedisPoolConfig.setMaxWaitMillis(properties.getMaxWait());
        return jedisPoolConfig;
    }
}
