package io.github.livenne.annotation.orm;

import io.github.livenne.MatchType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Cond {
    String value();
    MatchType type() default MatchType.EQUAL;
}
