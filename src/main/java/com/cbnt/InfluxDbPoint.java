package com.cbnt;

import org.influxdb.dto.Point;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

public class InfluxDbPoint {
  private Map<String, Object> fields = new HashMap<String, Object>();
  private Map<String, String> tags = new HashMap<String, String>();
  private ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
  private Long ts = (now.toInstant().getEpochSecond() * 1000000000L) + now.toInstant().getNano();
  private final static List<String> defaultTags = Arrays.asList("level", "loggerName", "millis", "source.className", "source.lineNumber", "source.fileName", "source.methodName", "threadId", "threadName", "threadPriority", "thrown.type");
  private final static List<String> defaultFields = Arrays.asList("message", "thrown.message", "thrown.stackTrace");
  private Map<String, String> includeFields = new HashMap<String, String>();
  private Map<String, String> includeTags = new HashMap<String, String>();
  private List<String> excludeFields = new ArrayList<>();
  private List<String> excludeTags = Arrays.asList("date");
  private Point point;

  public InfluxDbPoint(String measurement, Map<String, String> includeFields, Map<String, String> includeTags, List<String> excludeFields, List<String> excludeTags, Map<String, Object> data) {
    defaultFields.forEach(field -> {
      this.includeFields.put(field, field);
    });
    defaultTags.forEach(tag -> {
      this.includeTags.put(tag, tag);
    });
    this.includeFields.putAll(includeFields);
    this.includeTags.putAll(includeTags);
    this.excludeFields.addAll(excludeFields);
    this.excludeTags.addAll(excludeTags);

    convertMapToPoint(null, data);
    this.point = Point.measurement(measurement)
            .time(ts, TimeUnit.NANOSECONDS)
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
      if (value == null) {
        continue;
      }
      if (value instanceof Map<?, ?>) {
        convertMapToPoint(key, (Map<String, Object>) value);
      } else if (value instanceof Iterable) {
        continue;
      } else if (this.includeFields.containsKey(key)) {
        if (!this.excludeTags.contains(key)) {
          addField(this.includeFields.get(key), value);
        }
      } else if (this.includeTags.containsKey(key)) {
        if (!this.excludeTags.contains(key)) {
          addTag(this.includeTags.get(key), value.toString());
        }
      } else {
        if (value instanceof Number || value instanceof Boolean) {
          if (!this.excludeFields.contains(key)) {
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
