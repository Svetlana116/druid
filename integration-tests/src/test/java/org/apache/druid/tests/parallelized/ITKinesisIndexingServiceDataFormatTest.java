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

package org.apache.druid.tests.parallelized;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.apache.druid.guice.annotations.Json;
import org.apache.druid.testing.guice.DruidTestModuleFactory;
import org.apache.druid.tests.TestGroup;
import org.apache.druid.tests.indexer.AbstractKinesisIndexingServiceTest;
import org.apache.druid.tests.indexer.AbstractStreamIndexingTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testng.annotations.Guice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(TestGroup.KINESIS_DATA_FORMAT)
@Guice(moduleFactory = DruidTestModuleFactory.class)
public class ITKinesisIndexingServiceDataFormatTest extends AbstractKinesisIndexingServiceTest
{
  /**
   * Generates test parameters based on the given resources. The resources should be structured as
   *
   * <pre>{@code
   * {RESOURCES_ROOT}/stream/data/{DATA_FORMAT}/serializer
   *                                           /input_format
   *                                           /parser
   * }</pre>
   *
   * The {@code serializer} directory contains the spec of {@link org.apache.druid.testing.utils.EventSerializer} and
   * must be present. Either {@code input_format} or {@code parser} directory should be present if {@code serializer}
   * is present.
   */
  //@DataProvider(parallel = true)
  public static Stream<Arguments> resources() throws IOException
  {
    final List<Arguments> resources = new ArrayList<>();
    final List<String> dataFormats = listDataFormatResources();
    for (String eachFormat : dataFormats) {
      final Map<String, String> spec = findTestSpecs(String.join("/", DATA_RESOURCE_ROOT, eachFormat));
      final String serializerPath = spec.get(AbstractStreamIndexingTest.SERIALIZER);
      spec.forEach((k, path) -> {
        if (!AbstractStreamIndexingTest.SERIALIZER.equals(k)) {
          resources.add(Arguments.of(serializerPath, k, path));
        }
      });
    }

    return resources.stream();
  }

  @Inject
  private @Json
  ObjectMapper jsonMapper;

  @BeforeAll
  void beforeClass() throws Exception
  {
    doBeforeClass();
  }


  @ParameterizedTest
  @MethodSource("resources")
  void testIndexData(String serializerPath, String parserType, String specPath)
          throws Exception
  {
    doTestIndexDataStableState(null, serializerPath, parserType, specPath);
  }

  @Override
  public String getTestNamePrefix()
  {
    return "kinesis_data_format";
  }
}
