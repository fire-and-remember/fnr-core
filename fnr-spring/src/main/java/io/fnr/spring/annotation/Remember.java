package io.fnr.spring.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Remember {
    String jobName();
    long timeout() default 30;
    TimeUnit timeoutUnit() default TimeUnit.SECONDS;
}
