package org.apache.druid.testing.guice;

import com.google.inject.AbstractModule;

public class GuiceTestModule extends AbstractModule{

    static final String STRING = "abc";
    public static final int INT = 5;
    public static final Object OBJECT = new Object();
    static final String QUALIFIED = "qualifying";
    static final String BOUND = "binding";

    @Override
    protected void configure() {
        bind(String.class).toInstance(STRING);
        bind(int.class).toInstance(INT);
        bind(Object.class).toInstance(OBJECT);

        bind(String.class).annotatedWith(SomeBindingAnnotation.class).toInstance(BOUND);
        bind(String.class).annotatedWith(SomeQualifyingAnnotation.class).toInstance(QUALIFIED);
    }
}

