package org.apache.druid.tests;

import com.google.inject.Inject;
import org.apache.druid.testing.guice.GuiceTestModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.testing.guice.SomeQualifyingAnnotation;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@IncludeModule(GuiceTestModule.class)
public class DruidGuiceExtensionTest
{
  @Inject static int STATIC_INJECTION;
  @Inject
  Object memberInjection;
  @Inject
  @SomeQualifyingAnnotation
  String qualifiedField;

  @Disabled
  @Test
  void staticInjection() {
    assertThat(STATIC_INJECTION).isEqualTo(GuiceTestModule.INT);
  }

  @Disabled
  @Test
  void memberInjection() {
    assertThat(memberInjection).isEqualTo(GuiceTestModule.OBJECT);
  }

}
