package org.apache.druid.testing.guice;

import com.google.inject.Module;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD})
@Inherited
@Repeatable(IncludeModules.class)
@ExtendWith(GuiceExtension.class)
public @interface IncludeModule {
  Class<? extends Module>[] value();

//  Class<? extends IModuleFactory> moduleFactory() default IModuleFactory.class;
}
