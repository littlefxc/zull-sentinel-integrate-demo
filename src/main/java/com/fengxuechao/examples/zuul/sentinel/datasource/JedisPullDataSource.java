package com.fengxuechao.examples.zuul.sentinel.datasource;

import com.alibaba.csp.sentinel.datasource.AutoRefreshDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import redis.clients.jedis.JedisCluster;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/22
 */
public class JedisPullDataSource<T> extends AutoRefreshDataSource<String, T> {

    private final JedisCluster jedisCluster;

    private final String ruleKey;

    /**
     * Constructor of {@code JedisClusterDataSource}.
     *
     * @param jedisCluster JedisCluster
     * @param ruleKey      data key in Redis
     * @param parser       customized data parser, cannot be empty
     */
    public JedisPullDataSource(Converter<String, T> parser, JedisCluster jedisCluster, String ruleKey) {
        super(parser);
        AssertUtil.notNull(jedisCluster, "JedisCluster can not be null");
        AssertUtil.notEmpty(ruleKey, "Redis ruleKey can not be empty");
        this.jedisCluster = jedisCluster;
        this.ruleKey = ruleKey;
        loadInitialConfig();
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
}
