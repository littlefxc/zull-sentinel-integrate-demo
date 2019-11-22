package com.fengxuechao.examples.zuul.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/7/29
 */
@Data
@ConfigurationProperties(prefix = "redis.cluster")
public class RedisClusterProperties {

    /**
     * 是否允许这个redis config 生效
     */
    private boolean enabled = false;

    /**
     * redis 集群节点，以引文逗号分隔(,)
     */
    private String nodes;

    /**
     * 集群密码
     */
    private String password;

    /**
     * 超时时间, 毫秒为单位
     */
    private int timeout = 200;

    /**
     * 在集群情况下，一条redis命令执行时最多转发次数
     */
    private int maxRedirects = 5;

    /**
     * 出现异常最大重试次数
     */
    private int maxAttempts = 3;

    /**
     * 最大等待连接中的数量
     */
    private int maxIdle = 8;

    /**
     * 最小等待连接中的数量
     */
    private int minIdle = 0;

    /**
     * 连接池的最大数据库连接数
     */
    private int maxActive = 8;

    /**
     * 当池内没有返回对象时，最大等待时间
     */
    private long maxWait = -1;
}
