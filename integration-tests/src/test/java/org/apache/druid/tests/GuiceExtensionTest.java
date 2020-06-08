package org.apache.druid.tests;

import com.google.inject.Inject;
import org.apache.druid.testing.guice.GuiceTestModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.testing.guice.SomeQualifyingAnnotation;
import org.junit.jupiter.api.Test;

import static com.google.common.truth.Truth.assertThat;

@IncludeModule(GuiceTestModule.class)
public class GuiceExtensionTest
{
  @Inject static int STATIC_INJECTION;
  @Inject
  Object memberInjection;
  @Inject
  @SomeQualifyingAnnotation
  String qualifiedField;

  @Test
  void staticInjection() {
    assertThat(STATIC_INJECTION).isEqualTo(GuiceTestModule.INT);
  }

  @Test
  void memberInjection() {
    assertThat(memberInjection).isEqualTo(GuiceTestModule.OBJECT);
  }

}
