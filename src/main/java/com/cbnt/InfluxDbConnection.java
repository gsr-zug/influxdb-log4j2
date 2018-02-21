package com.cbnt;

import org.apache.logging.log4j.nosql.appender.DefaultNoSqlObject;
import org.apache.logging.log4j.nosql.appender.NoSqlConnection;
import org.apache.logging.log4j.nosql.appender.NoSqlObject;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The InfluxDB implementation of {@link NoSqlConnection}.
 */
public final class InfluxDbConnection implements NoSqlConnection<Map<String, Object>, DefaultNoSqlObject> {
  private InfluxDB influxDB;
  private String measurementName;
  private String databaseName;
  private String retentionPolicy;
  private Integer udpPort;

  public InfluxDbConnection(String databaseName, String measurementName, String retentionPolicy, String url, String username, String password, Boolean disableBatch, Integer batchActions, Integer batchDurationMs, Integer udpPort) {
    this.databaseName = databaseName;
    this.measurementName = measurementName;
    this.retentionPolicy = retentionPolicy;
    this.udpPort = udpPort;

    // Initialize InfluxDB object
    this.influxDB = InfluxDBFactory.connect(url, username, password);
    this.influxDB.setRetentionPolicy(this.retentionPolicy);
    this.influxDB.setConsistency(ConsistencyLevel.ONE);

    // create the database unless it already exists
    this.influxDB.createDatabase(this.databaseName);

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
    Point eventPoint = new InfluxDbPoint(this.measurementName, object.unwrap()).getPoint();
    if (this.udpPort != null) {
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
