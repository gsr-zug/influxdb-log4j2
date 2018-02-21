package com.cbnt;

import org.influxdb.dto.Point;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class InfluxDbPoint {
  private Map<String, Object> fields = new HashMap<String, Object>();
  private Map<String, String> tags = new HashMap<String, String>();
  private Long ts = ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli();
  private Point point;

  public InfluxDbPoint(String measurement, Map<String, Object> data) {
    convertMapToPoint(null, data);
    this.point = Point.measurement(measurement)
            .time(ts, TimeUnit.MILLISECONDS)
            .fields(this.fields)
            .tag(this.tags)
            .build();
  }

  @SuppressWarnings("unchecked")
  private void convertMapToPoint(String parent, Map<String, Object> map) {
    for (Entry<String, Object> entry : map.entrySet()) {
      String key = entry.getKey();
      if (parent != null) {
        key = parent + "." + key;
      }
      Object value = entry.getValue();
      if (value == null || value == "") {
        continue;
      }
      if (value instanceof Map<?, ?>) {
        convertMapToPoint(key, (Map<String, Object>) value);
      } else if (key.equals("date")) {
        this.ts = LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern("eee MMM dd HH:mm:ss zzz uuuu")).atZone(ZoneOffset.UTC).toInstant().toEpochMilli();
      } else if (value instanceof Iterable) {
        // ignore any iterable types
      } else if (value instanceof Number) {
        addField(key, value);
      } else {
        addTag(key, value.toString());
      }
    }
  }

  public Point getPoint() {
    return this.point;
  }

  private void addTag(String k, String v) {
    this.tags.put(k, v);
  }

  private void addField(String k, Object v) {
    this.fields.put(k, v);
  }
}
