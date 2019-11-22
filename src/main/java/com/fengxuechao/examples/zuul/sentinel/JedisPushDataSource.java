package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;

/**
 * TODO 订阅频道时会阻塞程序的启动
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/21
 */
@Slf4j
public class JedisPushDataSource<T> extends AbstractDataSource<String, T> {

    private final JedisCluster jedisCluster;

    private final String ruleKey;

    /**
     * Constructor of {@code JedisClusterDataSource}.
     *
     * @param jedisCluster JedisCluster
     * @param ruleKey      data key in Redis
     * @param channel      channel to subscribe in Redis
     * @param parser       customized data parser, cannot be empty
     */
    public JedisPushDataSource(Converter<String, T> parser, JedisCluster jedisCluster, String ruleKey, String channel) {
        super(parser);
        AssertUtil.notNull(jedisCluster, "JedisCluster can not be null");
        AssertUtil.notEmpty(ruleKey, "Redis ruleKey can not be empty");
        AssertUtil.notEmpty(channel, "Redis subscribe channel can not be empty");
        this.jedisCluster = jedisCluster;
        this.ruleKey = ruleKey;
        loadInitialConfig();
        subscribeFromChannel(channel);
    }

    private void subscribeFromChannel(String channel) {
        jedisCluster.subscribe(new DelegatingRedisPubSubListener(), channel);
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[RedisDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[RedisDataSource] Error when loading initial config", ex);
        }
    }

    /**
     * Read original data from the data source.
     *
     * @return the original data.
     * @throws Exception IO or other error occurs
     */
    @Override
    public String readSource() throws Exception {
        if (this.jedisCluster == null) {
            throw new IllegalStateException("JedisCluster has not been initialized or error occurred");
        }
        return jedisCluster.get(ruleKey);
    }

    /**
     * Close the data source.
     *
     * @throws Exception IO or other error occurs
     */
    @Override
    public void close() throws Exception {
        jedisCluster.close();
    }

    private class DelegatingRedisPubSubListener extends JedisPubSub {

        DelegatingRedisPubSubListener() {
        }

        @Override
        public void onMessage(String channel, String message) {
            RecordLog.info(String.format("[JedisClusterDataSource] New property value received for channel %s: %s", channel, message));
            log.info("[JedisClusterDataSource] New property value received for channel {}: {}", channel, message);
            getProperty().updateValue(parser.convert(message));
        }
    }
}
