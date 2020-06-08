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

import com.google.common.collect.ImmutableList;
import org.apache.druid.java.util.common.Pair;
import org.apache.druid.testing.guice.GuiceTestModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.druid.tests.TestGroup.S3_DEEP_STORAGE;

/**
 * IMPORTANT:
 * To run this test, you must:
 * 1) Run the test with -Dstart.hadoop.docker=true in the mvn command
 * 2) Provide -Doverride.config.path=<PATH_TO_FILE> with s3 credentials/configs set. See
 *    integration-tests/docker/environment-configs/override-examples/s3 for env vars to provide.
 *    You will also need to include "druid-hdfs-storage" to druid_extensions_loadList in this file.
 */

@Tag(S3_DEEP_STORAGE)
@IncludeModule(GuiceTestModule.class)
public class ITHdfsToS3ParallelIndexTest extends AbstractHdfsInputSourceParallelIndexTest
{
  private static final String INPUT_SOURCE_PATHS_KEY = "paths";

  @ParameterizedTest
  @MethodSource("resources")
  void testHdfsIndexData(Pair<String, List> hdfsInputSource) throws Exception
  {
    doTest(hdfsInputSource, InputFormatDetails.JSON);
  }

  public static Stream<Arguments> resources ()  {
    return Stream.of(
            Arguments.of(new Pair<>(INPUT_SOURCE_PATHS_KEY,
                    "hdfs://druid-it-hadoop:9000/batch_index%%FOLDER_SUFFIX%%"
            )),
            Arguments.of(new Pair<>(INPUT_SOURCE_PATHS_KEY,
                    ImmutableList.of(
                            "hdfs://druid-it-hadoop:9000/batch_index%%FOLDER_SUFFIX%%"
                    )
            )),
            Arguments.of(new Pair<>(INPUT_SOURCE_PATHS_KEY,
                    ImmutableList.of(
                            "hdfs://druid-it-hadoop:9000/batch_index%%FOLDER_SUFFIX%%/wikipedia_index_data1%%FILE_EXTENSION%%",
                            "hdfs://druid-it-hadoop:9000/batch_index%%FOLDER_SUFFIX%%/wikipedia_index_data2%%FILE_EXTENSION%%",
                            "hdfs://druid-it-hadoop:9000/batch_index%%FOLDER_SUFFIX%%/wikipedia_index_data3%%FILE_EXTENSION%%"
                    )
            )));
  }
}
