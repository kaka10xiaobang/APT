package com.kaka.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.lang.model.element.TypeElement;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface TestBindView {
    int value();

    String SUFFIX = "_TestBinding";
}
