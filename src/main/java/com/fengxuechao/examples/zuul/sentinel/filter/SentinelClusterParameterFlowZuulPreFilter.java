package com.fengxuechao.examples.zuul.sentinel.filter;

import com.alibaba.csp.sentinel.*;
import com.alibaba.csp.sentinel.adapter.gateway.common.param.GatewayParamParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.RequestContextItemParser;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.constants.ZuulConstant;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.BlockResponse;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback.ZuulBlockFallbackProvider;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import java.util.ArrayDeque;
import java.util.Deque;

import static com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants.GATEWAY_CONTEXT_ROUTE_PREFIX;

/**
 * 热点参数限流
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
public class SentinelClusterParameterFlowZuulPreFilter extends ZuulFilter {

    private final int order;

    private final GatewayParamParser<RequestContext> paramParser = new GatewayParamParser<>(
            new RequestContextItemParser());

    public SentinelClusterParameterFlowZuulPreFilter() {
        this(10000);
    }

    public SentinelClusterParameterFlowZuulPreFilter(int order) {
        this.order = order;
    }

    @Override
    public String filterType() {
        return ZuulConstant.PRE_TYPE;
    }

    /**
     * This run before route filter so we can get more accurate RT time.
     */
    @Override
    public int filterOrder() {
        return order;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    private void doSentinelEntry(String resourceName, RequestContext requestContext,
                                 Deque<EntryHolder> holders) throws BlockException {
        Object[] params = new Object[1];
        params[0] = requestContext.getRequest().getParameter("appId");
        AsyncEntry entry = SphU.asyncEntry(resourceName, ResourceTypeConstants.COMMON_WEB, EntryType.IN, params);
//        Entry entry = SphU.entry(resourceName, EntryType.OUT, 1, params);
        EntryHolder holder = new EntryHolder(entry, params);
        holders.push(holder);
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        String routeId = (String) ctx.get(ZuulConstant.PROXY_ID_KEY);

        Deque<EntryHolder> holders = new ArrayDeque<>();
        try {
            ContextUtil.enter(GATEWAY_CONTEXT_ROUTE_PREFIX + routeId);
            doSentinelEntry(routeId, ctx, holders);
        } catch (BlockException ex) {
            ZuulBlockFallbackProvider zuulBlockFallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(routeId);
            BlockResponse blockResponse = zuulBlockFallbackProvider.fallbackResponse(routeId, ex);
            // Prevent routing from running
            ctx.setRouteHost(null);
            ctx.set(ZuulConstant.SERVICE_ID_KEY, null);

            // Set fallback response.
            ctx.setResponseBody(blockResponse.toString());
            ctx.setResponseStatusCode(blockResponse.getCode());
            // Set Response ContentType
            ctx.getResponse().setContentType("application/json; charset=utf-8");
        } finally {
            // We don't exit the entry here. We need to exit the entries in post filter to record Rt correctly.
            // So here the entries will be carried in the request context.
            if (!holders.isEmpty()) {
                ctx.put(ZuulConstant.ZUUL_CTX_SENTINEL_ENTRIES_KEY, holders);
            }
        }
        return null;
    }
}
