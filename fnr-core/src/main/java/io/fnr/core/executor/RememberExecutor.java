package io.fnr.core.executor;

import io.fnr.core.domain.Ticket;
import io.fnr.core.domain.TicketResult;
import io.fnr.core.service.FnrTicketService;

import java.util.concurrent.Callable;

public interface RememberExecutor extends FnrTicketService {
    <T> Ticket<T> submit(String jobName, long timeoutSeconds, Object[] params, Class<T> resultType, Callable<T> task);
}
