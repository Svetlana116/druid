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

package org.apache.druid.tests.hadoop;

import com.google.common.collect.ImmutableList;
import org.apache.druid.indexer.partitions.DimensionBasedPartitionsSpec;
import org.apache.druid.indexer.partitions.HashedPartitionsSpec;
import org.apache.druid.indexer.partitions.SingleDimensionPartitionsSpec;
import org.apache.druid.java.util.common.StringUtils;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.testing.guice.GuiceTestModule;
import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.tests.indexer.AbstractITBatchIndexTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.Closeable;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.apache.druid.tests.TestGroup.HDFS_DEEP_STORAGE;

/**
 * IMPORTANT:
 * To run this test, you must:
 * 1) Copy wikipedia_index_data1.json, wikipedia_index_data2.json, and wikipedia_index_data3.json
 *    located in integration-tests/src/test/resources/data/batch_index/json to your HDFS at /batch_index/json/
 *    If using the Docker-based Hadoop container, this is automatically done by the integration tests.
 * 2) Copy batch_hadoop.data located in integration-tests/src/test/resources/data/batch_index/tsv to your HDFS
 *    at /batch_index/tsv/
 *    If using the Docker-based Hadoop container, this is automatically done by the integration tests.
 * 2) Provide -Doverride.config.path=<PATH_TO_FILE> with HDFS configs set. See
 *    integration-tests/docker/environment-configs/override-examples/hdfs for env vars to provide.
 * 3) Run the test with -Dstart.hadoop.docker=true -Dextra.datasource.name.suffix='' in the mvn command
 */
@Tag(HDFS_DEEP_STORAGE)
@IncludeModule(GuiceTestModule.class)
public class ITHadoopIndexTest extends AbstractITBatchIndexTest
{
  private static final Logger LOG = new Logger(ITHadoopIndexTest.class);

  private static final String BATCH_TASK = "/hadoop/batch_hadoop_indexer.json";
  private static final String BATCH_QUERIES_RESOURCE = "/hadoop/batch_hadoop_queries.json";
  private static final String BATCH_DATASOURCE = "batchLegacyHadoop";

  private static final String INDEX_TASK = "/hadoop/wikipedia_hadoop_index_task.json";
  private static final String INDEX_QUERIES_RESOURCE = "/indexer/wikipedia_index_queries.json";
  private static final String INDEX_DATASOURCE = "wikipedia_hadoop_index_test";

  private static final String REINDEX_TASK = "/hadoop/wikipedia_hadoop_reindex_task.json";
  private static final String REINDEX_QUERIES_RESOURCE = "/indexer/wikipedia_reindex_queries.json";
  private static final String REINDEX_DATASOURCE = "wikipedia_hadoop_reindex_test";

  public static Stream<Arguments> resources()
  {
    return Stream.of(
            Arguments.of(new HashedPartitionsSpec(3, null, null)),
            Arguments.of(new HashedPartitionsSpec(null, 3, ImmutableList.of("page"))),
            Arguments.of(new HashedPartitionsSpec(null, 3, ImmutableList.of("page", "user"))),
            Arguments.of(new SingleDimensionPartitionsSpec(1000, null, null, false)),
            Arguments.of(new SingleDimensionPartitionsSpec(1000, null, "page", false)),
            Arguments.of(new SingleDimensionPartitionsSpec(1000, null, null, true)));

    //{new HashedPartitionsSpec(null, 3, null)} // this results in a bug where the segments have 0 rows

  }

  @Test
  void testLegacyITHadoopIndexTest() throws Exception
  {
    String indexDatasource = BATCH_DATASOURCE + "_" + UUID.randomUUID();
    try (
            final Closeable ignored0 = unloader(indexDatasource + config.getExtraDatasourceNameSuffix());
    ) {
      final Function<String, String> specPathsTransform = spec -> {
        try {
          String path = "/batch_index/tsv";
          spec = StringUtils.replace(
                  spec,
                  "%%INPUT_PATHS%%",
                  path
          );

          return spec;
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      };

      doIndexTest(
              indexDatasource,
              BATCH_TASK,
              specPathsTransform,
              BATCH_QUERIES_RESOURCE,
              false,
              true,
              true
      );
    }
  }

  @ParameterizedTest
  @MethodSource("resources")
  void testIndexData(DimensionBasedPartitionsSpec partitionsSpec) throws Exception
  {
    String indexDatasource = INDEX_DATASOURCE + "_" + UUID.randomUUID();
    String reindexDatasource = REINDEX_DATASOURCE + "_" + UUID.randomUUID();

    try (
            final Closeable ignored1 = unloader(indexDatasource + config.getExtraDatasourceNameSuffix());
            final Closeable ignored2 = unloader(reindexDatasource + config.getExtraDatasourceNameSuffix());
    ) {
      final Function<String, String> specPathsTransform = spec -> {
        try {
          String path = "/batch_index/json";
          spec = StringUtils.replace(
                  spec,
                  "%%INPUT_PATHS%%",
                  path
          );
          spec = StringUtils.replace(
                  spec,
                  "%%PARTITIONS_SPEC%%",
                  jsonMapper.writeValueAsString(partitionsSpec)
          );

          return spec;
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      };

      doIndexTest(
              indexDatasource,
              INDEX_TASK,
              specPathsTransform,
              INDEX_QUERIES_RESOURCE,
              false,
              true,
              true
      );

      doReindexTest(
              indexDatasource,
              reindexDatasource,
              REINDEX_TASK,
              REINDEX_QUERIES_RESOURCE
      );
    }
  }
}
