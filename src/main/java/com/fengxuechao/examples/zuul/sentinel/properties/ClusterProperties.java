package com.fengxuechao.examples.zuul.sentinel.properties;

import lombok.Data;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Data
public class ClusterProperties {

    private ClientProperties client = new ClientProperties();

    private ServerProperties server = new ServerProperties();

    private String clusterGroupKey = "dev:cluster_group";

    @Data
    public static class ClientProperties {

        /**
         * 集群限流 - 客户端请求超时时间
         */
        private String configKey = "dev:cluster_client_config";

        /**
         * 集群限流 - 服务端地址分配
         */
        private String assignConfigKey = "dev:cluster_client_assign_config";
    }

    @Data
    public static class ServerProperties {

        /**
         * 服务端
         */
        private String transportConfigKey = "dev:cluster_server_transport_config";
    }
}
