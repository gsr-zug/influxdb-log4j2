package com.cbnt;

import org.apache.logging.log4j.core.appender.nosql.DefaultNoSqlObject;
import org.apache.logging.log4j.core.appender.nosql.NoSqlConnection;
import org.apache.logging.log4j.core.appender.nosql.NoSqlObject;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The InfluxDB implementation of {@link NoSqlConnection}.
 */
public final class InfluxDbConnection implements NoSqlConnection<Map<String, Object>, DefaultNoSqlObject> {
  private InfluxDB influxDB;
  private String measurement;
  private Integer udpPort;
  private Map<String, String> includeFields;
  private Map<String, String> includeTags;
  private List<String> excludeFields;
  private List<String> excludeTags;

  public InfluxDbConnection(String database, String measurement, String retentionPolicy, String url, String username, String password, Boolean disableBatch, Integer batchActions, Integer batchDurationMs, Integer udpPort, Map<String, String> includeFields, Map<String, String> includeTags, List<String> excludeFields, List<String> excludeTags, Boolean lazyInit) {
    this.measurement = measurement;
    this.udpPort = udpPort;
    this.includeFields = includeFields;
    this.includeTags = includeTags;
    this.excludeFields = excludeFields;
    this.excludeTags = excludeTags;

    // Initialize InfluxDB object
    this.influxDB = InfluxDBFactory.connect(url, username, password);
    this.influxDB.setDatabase(database);
    this.influxDB.setRetentionPolicy(retentionPolicy);
    this.influxDB.setConsistency(ConsistencyLevel.ONE);

    // create the database unless it already exists
    if (!lazyInit) {
      this.influxDB.createDatabase(database);
    }

      // enable batch mode
    if (!disableBatch) {
      this.influxDB.enableBatch(batchActions, batchDurationMs, TimeUnit.MILLISECONDS);
    }
  }

  @Override
  public DefaultNoSqlObject createObject() {
    return new DefaultNoSqlObject();
  }

  @Override
  public DefaultNoSqlObject[] createList(int length) {
    return new DefaultNoSqlObject[length];
  }

  @Override
  public void insertObject(NoSqlObject<Map<String, Object>> object) {
    // build event point
    Point eventPoint = new InfluxDbPoint(this.measurement, this.includeFields, this.includeTags, this.excludeFields, this.excludeTags, object.unwrap()).getPoint();
    System.out.println(String.format("UDP port is: %d", this.udpPort));
    if (this.udpPort > 0) {
      // send using UDP
      this.influxDB.write(this.udpPort, eventPoint);
    } else {
      // send using standard interface (HTTP)
      this.influxDB.write(eventPoint);
    }
  }

  @Override
  public void close() {
    // Do nothing...
  }

  @Override
  public boolean isClosed() {
    return false;
  }

}
