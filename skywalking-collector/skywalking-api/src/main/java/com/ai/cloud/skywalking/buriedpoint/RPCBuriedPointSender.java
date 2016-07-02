package com.ai.cloud.skywalking.buriedpoint;

import com.ai.cloud.skywalking.api.IBuriedPointSender;
import com.ai.cloud.skywalking.conf.AuthDesc;
import com.ai.cloud.skywalking.context.Context;
import com.ai.cloud.skywalking.logging.LogManager;
import com.ai.cloud.skywalking.logging.Logger;
import com.ai.cloud.skywalking.model.ContextData;
import com.ai.cloud.skywalking.model.EmptyContextData;
import com.ai.cloud.skywalking.model.Identification;
import com.ai.cloud.skywalking.protocol.Span;
import com.ai.cloud.skywalking.protocol.SpanType;
import com.ai.cloud.skywalking.util.ContextGenerator;

public class RPCBuriedPointSender extends BuriedPointInvoker implements IBuriedPointSender {

    private static Logger logger = LogManager
            .getLogger(RPCBuriedPointSender.class);

    @Override
    public ContextData beforeSend(Identification id) {
        try {
            if (!AuthDesc.isAuth())
                return new EmptyContextData();

            Span spanData = ContextGenerator.generateSpanFromThreadLocal(id);
            //设置SpanType的类型
            spanData.setSpanType(SpanType.RPC_CLIENT);

            Context.append(spanData);

            return new ContextData(spanData.getTraceId(), generateSubParentLevelId(spanData), spanData.getCallType());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            return new EmptyContextData();
        }
    }

    private String generateSubParentLevelId(Span spanData) {
        if (spanData.getParentLevel() == null) {
            return spanData.getLevelId() + "";
        }

        return spanData.getParentLevel() + "." + spanData.getLevelId();
    }

    @Override
    public void afterSend() {
        super.afterInvoker();
    }
}
