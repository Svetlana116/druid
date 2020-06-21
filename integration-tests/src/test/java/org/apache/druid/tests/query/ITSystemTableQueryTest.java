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

package org.apache.druid.tests.query;

import com.google.inject.Inject;
import org.apache.druid.testing.IntegrationTestingConfig;
import org.apache.druid.testing.clients.CoordinatorResourceTestClient;
import org.apache.druid.testing.guice.DruidGuiceExtension;
import org.apache.druid.testing.guice.IncludeModule;
import org.apache.druid.testing.utils.ITRetryUtil;
import org.apache.druid.testing.utils.SqlTestQueryHelper;
import org.apache.druid.tests.TestGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag(TestGroup.QUERY)
@IncludeModule(DruidGuiceExtension.TestModule.class)
public class ITSystemTableQueryTest
{
  private static final String WIKIPEDIA_DATA_SOURCE = "wikipedia_editstream";
  private static final String TWITTER_DATA_SOURCE = "twitterstream";
  private static final String SYSTEM_QUERIES_RESOURCE = "/queries/sys_segment_queries.json";

  @Inject
  CoordinatorResourceTestClient coordinatorClient;
  @Inject
  private SqlTestQueryHelper queryHelper;
  @Inject
  IntegrationTestingConfig config;

  @BeforeEach
  void before()
  {
    // ensure that wikipedia segments are loaded completely
    ITRetryUtil.retryUntilTrue(
        () -> coordinatorClient.areSegmentsLoaded(WIKIPEDIA_DATA_SOURCE), "wikipedia segment load"
    );

    // ensure that the twitter segments are loaded completely
    ITRetryUtil.retryUntilTrue(
        () -> coordinatorClient.areSegmentsLoaded(TWITTER_DATA_SOURCE), "twitter segment load"
    );
  }

  @Test
  void testSystemTableQueries()
  {
    try {
      this.queryHelper.testQueriesFromFile(SYSTEM_QUERIES_RESOURCE, 2);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
