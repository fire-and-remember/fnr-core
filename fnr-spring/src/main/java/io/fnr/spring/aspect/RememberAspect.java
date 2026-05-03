package io.fnr.spring.aspect;

import io.fnr.core.domain.Ticket;
import io.fnr.core.executor.RememberExecutor;
import io.fnr.spring.annotation.Remember;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class RememberAspect {

    private final RememberExecutor executor;

    public RememberAspect(RememberExecutor executor) {
        this.executor = executor;
    }

    @Around("@annotation(io.fnr.spring.annotation.Remember)")
    @SuppressWarnings("unchecked")
    public Object around(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Remember annotation = method.getAnnotation(Remember.class);

        if (!Ticket.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(
                "@Remember method must return Ticket<?>: " + method.getName()
            );
        }

        ParameterizedType genericReturnType = (ParameterizedType) method.getGenericReturnType();
        Class<Object> resultType = (Class<Object>) genericReturnType.getActualTypeArguments()[0];

        long timeoutSeconds = annotation.timeoutUnit().toSeconds(annotation.timeout());
        return executor.submit(annotation.jobName(), timeoutSeconds, joinPoint.getArgs(), resultType, () -> {
            try {
                Object proceeded = joinPoint.proceed();
                if (proceeded instanceof Ticket<?> ticket) {
                    return (Object) ticket.getValue();
                }
                return null;
            } catch (Throwable t) {
                throw new Exception(t);
            }
        });
    }
}
