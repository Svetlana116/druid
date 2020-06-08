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
import com.google.common.collect.ImmutableMap;
import org.apache.druid.java.util.common.Pair;
import org.apache.druid.testing.guice.GuiceTestModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.apache.druid.tests.TestNGGroup.HDFS_DEEP_STORAGE;

/**
 * IMPORTANT:
 * To run this test, you must:
 * 1) Set the bucket and path for your data. This can be done by setting -Ddruid.test.config.cloudBucket and
 *    -Ddruid.test.config.cloudPath or setting "cloud_bucket" and "cloud_path" in the config file.
 * 2) Copy wikipedia_index_data1.json, wikipedia_index_data2.json, and wikipedia_index_data3.json
 *    located in integration-tests/src/test/resources/data/batch_index/json to your GCS at the location set in step 1.
 * 3) Provide -Doverride.config.path=<PATH_TO_FILE> with gcs configs and hdfs deep storage configs set. See
 *    integration-tests/docker/environment-configs/override-examples/gcs and
 *    integration-tests/docker/environment-configs/override-examples/hdfs for env vars to provide.
 * 4) Provide -Dresource.file.dir.path=<PATH_TO_FOLDER> with folder that contains GOOGLE_APPLICATION_CREDENTIALS file
 * 5) Run the test with -Dstart.hadoop.docker=true in the mvn command
 */
@Tag(HDFS_DEEP_STORAGE)
@IncludeModule(GuiceTestModule.class)
public class ITGcsToHdfsParallelIndexTest extends AbstractGcsInputSourceParallelIndexTest
{
  private static final String INPUT_SOURCE_URIS_KEY = "uris";
  private static final String INPUT_SOURCE_PREFIXES_KEY = "prefixes";
  private static final String INPUT_SOURCE_OBJECTS_KEY = "objects";
  private static final String WIKIPEDIA_DATA_1 = "wikipedia_index_data1.json";
  private static final String WIKIPEDIA_DATA_2 = "wikipedia_index_data2.json";
  private static final String WIKIPEDIA_DATA_3 = "wikipedia_index_data3.json";

  public static Stream<Arguments> resources()  {
    return Stream.of(
            Arguments.of(new Pair<>(INPUT_SOURCE_URIS_KEY,
                    ImmutableList.of(
                            "gs://%%BUCKET%%/%%PATH%%" + WIKIPEDIA_DATA_1,
                            "gs://%%BUCKET%%/%%PATH%%" + WIKIPEDIA_DATA_2,
                            "gs://%%BUCKET%%/%%PATH%%" + WIKIPEDIA_DATA_3
                    )
            )),
            Arguments.of(new Pair<>(INPUT_SOURCE_PREFIXES_KEY,
                    ImmutableList.of(
                            "gs://%%BUCKET%%/%%PATH%%"
                    )
            )),
            Arguments.of(new Pair<>(INPUT_SOURCE_OBJECTS_KEY,
                    ImmutableList.of(
                            ImmutableMap.of("bucket", "%%BUCKET%%", "path", "%%PATH%%" + WIKIPEDIA_DATA_1),
                            ImmutableMap.of("bucket", "%%BUCKET%%", "path", "%%PATH%%" + WIKIPEDIA_DATA_2),
                            ImmutableMap.of("bucket", "%%BUCKET%%", "path", "%%PATH%%" + WIKIPEDIA_DATA_3)
                    )
            )));
  }

  @ParameterizedTest
  @MethodSource("resources")
  void testGcsIndexData(Pair<String, List> gcsInputSource) throws Exception
  {
    doTest(gcsInputSource);
  }
}
