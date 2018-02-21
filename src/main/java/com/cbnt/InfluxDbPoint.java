package com.cbnt;

import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class InfluxDbPoint {
  private Map<String, Object> fields = new HashMap<String, Object>();
  private Map<String, String> tags = new HashMap<String, String>();
  private Point point;

  public InfluxDbPoint(String measurementName, Map<String, Object> data) {
    convertMapToPoint(null, data);
    this.point = Point.measurement(measurementName)
            .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
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