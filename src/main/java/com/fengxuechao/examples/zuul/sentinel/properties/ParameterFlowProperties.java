package com.fengxuechao.examples.zuul.sentinel.properties;

import lombok.Data;

/**
 * 热点参数限流配置
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Data
public class ParameterFlowProperties {

    /**
     * 开启热点参数限流
     */
    private Boolean enabled = false;

    /**
     * 热点参数限流缓存 Key
     */
    private String key = "dev:parameter_flow";
}
