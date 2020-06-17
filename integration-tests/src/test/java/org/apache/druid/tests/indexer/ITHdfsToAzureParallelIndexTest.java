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

import org.apache.druid.java.util.common.Pair;
import org.apache.druid.testing.guice.DruidTestModuleFactory;
import org.apache.druid.tests.TestGroup;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.testng.annotations.Guice;

import java.util.List;

/**
 * IMPORTANT:
 * To run this test, you must:
 * 1) Run the test with -Dstart.hadoop.docker=true in the mvn command
 * 2) Provide -Doverride.config.path=<PATH_TO_FILE> with Azure credentials/configs set. See
 *    integration-tests/docker/environment-configs/override-examples/azure for env vars to provide.
 *    You will also need to include "druid-hdfs-storage" to druid_extensions_loadList in this file.
 */
@Tag(TestGroup.AZURE_DEEP_STORAGE)
@Guice(moduleFactory = DruidTestModuleFactory.class)
public class ITHdfsToAzureParallelIndexTest extends AbstractHdfsInputSourceParallelIndexTest
{
  @ParameterizedTest
  @ArgumentsSource(AbstractHdfsInputSourceParallelIndexTest.class)
  void testHdfsIndexData(Pair<String, List> hdfsInputSource) throws Exception
  {
    doTest(hdfsInputSource, InputFormatDetails.JSON);
  }
}
