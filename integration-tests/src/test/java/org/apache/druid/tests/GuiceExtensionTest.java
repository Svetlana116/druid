package org.apache.druid.tests;

import com.google.inject.AbstractModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.testing.guice.SomeBindingAnnotation;
import org.apache.druid.testing.guice.SomeQualifyingAnnotation;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;
import javax.inject.Inject;



@IncludeModule(GuiceExtensionTest.TestModule.class)
public class GuiceExtensionTest
{
  @Inject static int STATIC_INJECTION;
  @Inject
  Object memberInjection;
  @Inject @SomeQualifyingAnnotation
  String qualifiedField;

  @Test
  void staticInjection() {
    assertThat(STATIC_INJECTION).isEqualTo(TestModule.INT);
  }

  @Test
  void memberInjection() {
    assertThat(memberInjection).isEqualTo(TestModule.OBJECT);
  }

  public static final class TestModule extends AbstractModule
  {
    static final String STRING = "abc";
    static final int INT = 5;
    static final Object OBJECT = new Object();
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
}
