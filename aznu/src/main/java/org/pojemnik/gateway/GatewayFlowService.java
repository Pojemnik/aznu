package org.pojemnik.gateway;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GatewayFlowService
{
    @Data
    private static class FlowState
    {
        private boolean paymentProcessed = false;
        private boolean ticketReserved = false;

        public boolean isFinished()
        {
            return paymentProcessed && ticketReserved;
        }
    }

    private final Map<String, FlowState> flowStates = new HashMap<>();

    public synchronized boolean onPaymentProcessed(String transactionId)
    {
        FlowState state = flowStates.getOrDefault(transactionId, new FlowState());
        state.setPaymentProcessed(true);
        flowStates.put(transactionId, state);
        return state.isFinished();
    }

    public synchronized boolean onTicketReserved(String transactionId)
    {
        FlowState state = flowStates.getOrDefault(transactionId, new FlowState());
        state.setTicketReserved(true);
        flowStates.put(transactionId, state);
        return state.isFinished();
    }
}
