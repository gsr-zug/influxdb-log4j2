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
  private String measurement;
  private Integer udpPort;
  private Boolean enabled;

  public InfluxDbConnection(String database, String measurement, String retentionPolicy, String url, String username, String password, Boolean disableBatch, Integer batchActions, Integer batchDurationMs, Integer udpPort, Boolean enabled) {
    this.measurement = measurement;
    this.udpPort = udpPort;
    this.enabled = enabled;

    // Initialize InfluxDB object
    if (enabled) {
      this.influxDB = InfluxDBFactory.connect(url, username, password);
      this.influxDB.setDatabase(database);
      this.influxDB.setRetentionPolicy(retentionPolicy);
      this.influxDB.setConsistency(ConsistencyLevel.ONE);

      // create the database unless it already exists
      this.influxDB.createDatabase(database);

      // enable batch mode
      if (!disableBatch) {
        this.influxDB.enableBatch(batchActions, batchDurationMs, TimeUnit.MILLISECONDS);
      }
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
    if (this.enabled) {
      // build event point
      Point eventPoint = new InfluxDbPoint(this.measurement, object.unwrap()).getPoint();
      if (this.udpPort > 0) {
        // send using UDP
        this.influxDB.write(this.udpPort, eventPoint);
      } else {
        // send using standard interface (HTTP)
        this.influxDB.write(eventPoint);
      }
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
