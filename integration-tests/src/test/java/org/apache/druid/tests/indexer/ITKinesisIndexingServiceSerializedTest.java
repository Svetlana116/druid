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

package org.apache.druid.tests.indexer;

import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.tests.GuiceExtensionTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.apache.druid.tests.TestNGGroup.KINESIS_INDEX;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(KINESIS_INDEX)
@IncludeModule(GuiceExtensionTest.TestModule.class)
public class ITKinesisIndexingServiceSerializedTest extends AbstractKinesisIndexingServiceTest
{
  @Override
  public String getTestNamePrefix()
  {
    return "kinesis_serialized";
  }

  @BeforeAll
  void beforeClass() throws Exception
  {
    doBeforeClass();
  }

  /**
   * This test must be run individually since the test affect and modify the state of the Druid cluster
   */
  @Test
  void testKinesisIndexDataWithLosingCoordinator() throws Exception
  {
    doTestIndexDataWithLosingCoordinator(null);
  }

  /**
   * This test must be run individually since the test affect and modify the state of the Druid cluster
   */
  @Test
  void testKinesisIndexDataWithLosingOverlord() throws Exception
  {
    doTestIndexDataWithLosingOverlord(null);
  }

  /**
   * This test must be run individually since the test affect and modify the state of the Druid cluster
   */
  @Test
  void testKinesisIndexDataWithLosingHistorical() throws Exception
  {
    doTestIndexDataWithLosingHistorical(null);
  }
}
