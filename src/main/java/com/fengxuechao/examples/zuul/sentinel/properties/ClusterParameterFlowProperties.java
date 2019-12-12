package com.fengxuechao.examples.zuul.sentinel.properties;

import lombok.Data;

/**
 * 集群限流 - 热点参数配置
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Data
public class ClusterParameterFlowProperties {

    /**
     * 开启集群限流 - 热点参数
     */
    private Boolean enabled = false;
}
