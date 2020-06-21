/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.testing.guice;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.TypeToken;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.ProvisionException;
import org.apache.druid.guice.GuiceInjectors;
import org.apache.druid.guice.IndexingServiceFirehoseModule;
import org.apache.druid.guice.IndexingServiceInputSourceModule;
import org.apache.druid.initialization.Initialization;
import org.apache.druid.testing.IntegrationTestingConfig;
import org.apache.druid.testing.utils.DruidClusterAdminClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;

import javax.inject.Qualifier;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;



public final class DruidGuiceExtension implements TestInstancePostProcessor, ParameterResolver
{
  private static final Namespace NAMESPACE =
      Namespace.create("name", "falgout", "jeffrey", "testing", "junit", "guice");

  private static final ConcurrentMap<Set<? extends Class<?>>, Injector> INJECTOR_CACHE = new ConcurrentHashMap<>();

  private static final Module MODULE = new DruidTestModule();
  private static final Injector INJECTOR = Initialization.makeInjectorWithModules(
          GuiceInjectors.makeStartupInjector(),
          getModules()
  );

  private static List<? extends Module> getModules()
  {
    return ImmutableList.of(
            new DruidTestModule(),
            new IndexingServiceFirehoseModule(),
            new IndexingServiceInputSourceModule()
    );
  }

  public static Injector getInjector()
  {
    return INJECTOR;
  }

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context)
  {
    getOrCreateInjector(context).ifPresent(injector -> injector.injectMembers(testInstance));
    waitUntilInstanceReady();
  }

  /**
   * Returns an injector for the given context if and only if the given context has an {@link
   * ExtensionContext#getElement() annotated element}.
   */
  private static Optional<Injector> getOrCreateInjector(ExtensionContext context)
  {
    if (!context.getElement().isPresent()) {
      return Optional.empty();
    }
    return Optional.of(getInjector());
  }

  /**
   * Returns module types that are introduced for the first time by the given context (they do not
   * appear in an enclosing context).
   */
  private static Set<Class<? extends Module>> getNewModuleTypes(ExtensionContext context)
  {
    if (!context.getElement().isPresent()) {
      return Collections.emptySet();
    }

    Set<Class<? extends Module>> moduleTypes = getModuleTypes(context.getElement().get());
    context.getParent()
        .map(DruidGuiceExtension::getContextModuleTypes)
        .ifPresent(moduleTypes::removeAll);

    return moduleTypes;
  }

  private static Set<Class<? extends Module>> getContextModuleTypes(ExtensionContext context)
  {
    return getContextModuleTypes(Optional.of(context));
  }

  /**
   * Returns module types that are present on the given context or any of its enclosing contexts.
   */
  private static Set<Class<? extends Module>> getContextModuleTypes(
      Optional<ExtensionContext> context)
  {
    // TODO: Cache?

    Set<Class<? extends Module>> contextModuleTypes = new LinkedHashSet<>();
    while (context.isPresent() && (hasAnnotatedElement(context) || hasParent(context))) {
      context
          .flatMap(ExtensionContext::getElement)
          .map(DruidGuiceExtension::getModuleTypes)
          .ifPresent(contextModuleTypes::addAll);
      context = context.flatMap(ExtensionContext::getParent);
    }

    return contextModuleTypes;
  }

  private static boolean hasAnnotatedElement(Optional<ExtensionContext> context)
  {
    return context.flatMap(ExtensionContext::getElement).isPresent();
  }

  private static boolean hasParent(Optional<ExtensionContext> context)
  {
    return context.flatMap(ExtensionContext::getParent).isPresent();
  }

  private static Set<Class<? extends Module>> getModuleTypes(AnnotatedElement element)
  {
    return
            AnnotationSupport.findRepeatableAnnotations(element, IncludeModule.class)
            .stream()
            .map(IncludeModule::value)
            .flatMap(Stream::of)
            .collect(Collectors.toSet());
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
                                   ExtensionContext extensionContext)
      throws ParameterResolutionException
  {
    Parameter parameter = parameterContext.getParameter();
    if (getBindingAnnotations(parameter).size() > 1) {
      return false;
    }

    Key<?> key = getKey(
        extensionContext.getTestClass(),
        parameter);
    Optional<Injector> optInjector = getInjectorForParameterResolution(extensionContext);
    return optInjector.filter(injector -> {

      // Do not bind String without explicit bindings.
      if (key.equals(Key.get(String.class)) && injector.getExistingBinding(key) == null) {
        return false;
      }

      try {
        injector.getInstance(key);
        return true;
      }
      catch (ConfigurationException | ProvisionException e) {
        // If we throw a ParameterResolutionException here instead of returning false, we'll block
        // other ParameterResolvers from being able to work.
        return false;
      }
    }).isPresent();
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
                                 ExtensionContext extensionContext)
      throws ParameterResolutionException
  {
    return null;
  }

  /**
   * Wrap {@link #getOrCreateInjector(ExtensionContext)} and rethrow exceptions as {@link
   * ParameterResolutionException}.
   */
  private static Optional<Injector> getInjectorForParameterResolution(
      ExtensionContext extensionContext) throws ParameterResolutionException
  {
    return getOrCreateInjector(extensionContext);
  }

  private static Key<?> getKey(Optional<Class<?>> containingElement, Parameter parameter)
  {
    Class<?> clazz =
        containingElement.orElseGet(() -> parameter.getDeclaringExecutable().getDeclaringClass());
    TypeToken<?> classType = TypeToken.of(clazz);
    Type resolvedType = classType.resolveType(parameter.getParameterizedType()).getType();

    Optional<Key<?>> key =
        getOnlyBindingAnnotation(parameter).map(annotation -> Key.get(resolvedType, annotation));
    return key.orElse(Key.get(resolvedType));
  }

  /**
   * @throws IllegalArgumentException if the given element has more than one binding
   *     annotation.
   */
  private static Optional<? extends Annotation> getOnlyBindingAnnotation(AnnotatedElement element)
  {
    return Optional.ofNullable(Iterables.getOnlyElement(getBindingAnnotations(element), null));
  }

  private static List<Annotation> getBindingAnnotations(AnnotatedElement element)
  {
    List<Annotation> annotations = new ArrayList<>();
    for (Annotation annotation : element.getAnnotations()) {
      if (isBindingAnnotation(annotation)) {
        annotations.add(annotation);
      }
    }

    return annotations;
  }

  private static boolean isBindingAnnotation(Annotation annotation)
  {
    Class<? extends Annotation> annotationType = annotation.annotationType();
    return annotationType.isAnnotationPresent(Qualifier.class)
        || annotationType.isAnnotationPresent(BindingAnnotation.class);
  }

  public void waitUntilInstanceReady()
  {
    DruidClusterAdminClient druidClusterAdminClient = getInjector().getInstance(DruidClusterAdminClient.class);
    IntegrationTestingConfig config = getInjector().getInstance(IntegrationTestingConfig.class);
    druidClusterAdminClient.waitUntilCoordinatorReady();
    druidClusterAdminClient.waitUntilIndexerReady();
    druidClusterAdminClient.waitUntilBrokerReady();
    String routerHost = config.getRouterUrl();
    if (null != routerHost) {
      druidClusterAdminClient.waitUntilRouterReady();
    }
  }

  public static final class TestModule extends AbstractModule
  {
    static final String STRING = "abc";
    static final int INT = 5;
    static final Object OBJECT = new Object();
    static final String QUALIFIED = "qualifying";
    static final String BOUND = "binding";

    @Override
    protected void configure()
    {
      bind(String.class).toInstance(STRING);
      bind(int.class).toInstance(INT);
      bind(Object.class).toInstance(OBJECT);

      bind(String.class).annotatedWith(SomeBindingAnnotation.class).toInstance(BOUND);
      bind(String.class).annotatedWith(SomeQualifyingAnnotation.class).toInstance(QUALIFIED);
    }
  }
}
