package com.fengxuechao.examples.zuul.sentinel.filter;

import com.alibaba.csp.sentinel.Entry;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
class EntryHolder {

    final private Entry entry;

    final private Object[] params;

    public EntryHolder(Entry entry, Object[] params) {
        this.entry = entry;
        this.params = params;
    }

    public Entry getEntry() {
        return entry;
    }

    public Object[] getParams() {
        return params;
    }
}
