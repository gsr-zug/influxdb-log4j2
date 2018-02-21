/*
* Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package com.cbnt;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.nosql.appender.NoSqlProvider;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * The InfluxDB implementation of {@link NoSqlProvider}.
 */
@Plugin(name = "InfluxDb", category = "Core", printObject = true)
public final class InfluxDbProvider implements NoSqlProvider<InfluxDbConnection> {

  private static final Logger LOGGER = StatusLogger.getLogger();

  private final String url;
  private final String username;
  private final String password;
  private final String database;
  private final String measurement;
  private final String retentionPolicy;
  private final String description;
  private final Boolean disableBatch;
  private final Integer batchActions;
  private final Integer batchDurationMs;
  private final Integer udpPort;
  private final Boolean enabled;

  public InfluxDbProvider(final String database, final String measurement,
                          final String retentionPolicy, final String url, final String username, final String password, Boolean disableBatch, final Integer batchActions, final Integer batchDurationMs, final Integer udpPort, final Boolean enabled) {
    this.database = database;
    this.measurement = measurement;
    this.url = url;
    this.username = username;
    this.password = password;
    this.retentionPolicy = retentionPolicy;
    this.disableBatch = disableBatch;
    this.batchActions = batchActions;
    this.batchDurationMs = batchDurationMs;
    this.udpPort = udpPort;
    this.enabled = enabled;
    this.description = "InfluxDbProvider [" + database + "]";
    validateConfiguration();
  }

  private void validateConfiguration() throws IllegalArgumentException {
  }

  @Override
  public InfluxDbConnection getConnection() {
    return new InfluxDbConnection(database, measurement, retentionPolicy, url, username, password, disableBatch, batchActions, batchDurationMs, udpPort, enabled);
  }

  @Override
  public String toString() {
    return this.description;
  }

  @PluginFactory
  public static InfluxDbProvider createNoSqlProvider(
          @PluginAttribute(value = "database") final String database,
          @PluginAttribute(value = "measurement") final String measurement,
          @PluginAttribute(value = "retentionPolicy", defaultString = "autogen") final String retentionPolicy,
          @PluginAttribute(value = "url") final String url,
          @PluginAttribute(value = "username") final String username,
          @PluginAttribute(value = "password", sensitive = true) final String password,
          @PluginAttribute(value = "disableBatch", defaultBoolean = true) final Boolean disableBatch,
          @PluginAttribute(value = "batchActions", defaultInt = 2000) final Integer batchActions,
          @PluginAttribute(value = "batchDurationMs", defaultInt = 100) final Integer batchDurationMs,
          @PluginAttribute(value = "udpPort", defaultInt = 0) final Integer udpPort,
          @PluginAttribute(value = "enabled", defaultBoolean = false) final Boolean enabled) {
    return new InfluxDbProvider(database, measurement, retentionPolicy, url, username, password, disableBatch, batchActions, batchDurationMs, udpPort, enabled);
  }
}
