package io.fnr.spring.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * Marks a method for async execution with automatic task tracking.
 *
 * <p>When a method annotated with {@code @Remember} is called, it runs asynchronously
 * and returns a {@link io.fnr.core.domain.Ticket} immediately. The actual result can be
 * retrieved at any time using {@link io.fnr.core.service.FnrTicketService}.
 *
 * <p>Requirements:
 * <ul>
 *   <li>The method must return {@code Ticket<T>}, where {@code T} is the actual return type.</li>
 *   <li>The bean must be managed by Spring (the method is intercepted via AOP).</li>
 * </ul>
 *
 * <pre>{@code
 * @Remember(jobName = "send-email", timeout = 30, timeoutUnit = TimeUnit.SECONDS)
 * public Ticket<EmailResult> sendEmail(EmailRequest request) {
 *     // long-running logic
 *     return null; // actual return value is captured by the framework
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Remember {

    /**
     * Logical name for this job, stored alongside the task record.
     * Use a consistent name to identify the job type across restarts.
     *
     * @return the job name
     */
    String jobName();

    /**
     * Maximum time allowed for the task to complete before it is considered timed out.
     * Defaults to {@code 30}.
     *
     * @return the timeout value
     * @see #timeoutUnit()
     */
    long timeout() default 30;

    /**
     * Unit for the {@link #timeout()} value.
     * Defaults to {@link TimeUnit#SECONDS}.
     *
     * @return the time unit
     */
    TimeUnit timeoutUnit() default TimeUnit.SECONDS;
}
