package com.cbnt;

import org.influxdb.dto.Point;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class InfluxDbPoint {
  private Map<String, Object> fields = new HashMap<String, Object>();
  private Map<String, String> tags = new HashMap<String, String>();
  private ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
  private Long ts = (now.toInstant().getEpochSecond() * 1000000000L) + now.toInstant().getNano();
  private final static List<String> defaultTags = Arrays.asList("level", "loggerName", "millis", "source.className", "source.fileName", "source.methodName", "threadId", "threadName", "threadPriority", "thrown.type");
  private final static List<String> defaultFields = Arrays.asList("message", "thrown.message", "thrown.stackTrace");
  private HashMap<String, String> includeFields = new HashMap<String, String>();
  private HashMap<String, String> includeTags = new HashMap<String, String>();
  private List<String> excludeFields;
  private List<String> excludeTags;
  private Point point;

  public InfluxDbPoint(String measurement, HashMap<String, String> includeFields, HashMap<String, String> includeTags, List<String> excludeFields, List<String> excludeTags, Map<String, Object> data) {
    convertMapToPoint(null, data);
    this.point = Point.measurement(measurement)
            .time(ts, TimeUnit.NANOSECONDS)
            .fields(this.fields)
            .tag(this.tags)
            .build();

    defaultFields.stream().map(x -> this.includeFields.put(x, x));
    defaultTags.stream().map(x -> this.includeTags.put(x, x));
    this.includeFields.putAll(includeTags);
    this.includeTags.putAll(includeFields);
    this.excludeFields = excludeFields;
    this.excludeTags = excludeTags;
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
        return;
      } else if (value instanceof Iterable) {
        return;
      } else if (this.includeFields.containsKey(key)) {
        if (!this.excludeTags.contains(key)) {
          addField(includeFields.get(key), value);
        }
      } else if (this.includeTags.containsKey(key)) {
        if (!this.excludeTags.contains(key)) {
          addTag(this.includeTags.get(key), value.toString());
        }
      } else {
        if (value instanceof Number || value instanceof Boolean) {
          if (this.excludeFields.contains(key)) {
            addField(key, value);
          }
        } else {
          if (!this.excludeTags.contains(key)) {
            addTag(key, value.toString());
          }
        }
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
