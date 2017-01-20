package org.radargun.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.radargun.utils.TimeService;

/**
 * Events that should be presented in report
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class Timeline implements Serializable, Comparable<Timeline> {

   public final int slaveIndex;
   private Map<Category, List<Event>> events = new HashMap<Category, List<Event>>();
   private long firstTimestamp = Long.MAX_VALUE;
   private long lastTimestamp = Long.MIN_VALUE;

   public Timeline(int slaveIndex) {
      this.slaveIndex = slaveIndex;
   }

   public static class Category implements Serializable, Comparable<Category> {
      private final String name;
      private final CategoryType type;

      public enum CategoryType {
         /* All events related to system resources (CPU, memory, network, etc.) */
         SYSMONITOR,
         /* Any other type of events, e.g. recording values in background stages */
         CUSTOM
      }

      private Category(String name, CategoryType type) {
         Objects.nonNull(name);
         Objects.nonNull(type);
         this.name = name;
         this.type = type;
      }

      @Override
      public int compareTo(Category other) {
         return this.getName().compareTo(other.getName());
      }

      public static Category sysCategory(String name) {
         return new Category(name, CategoryType.SYSMONITOR);
      }

      public static Category customCategory(String name) {
         return new Category(name, CategoryType.CUSTOM);
      }

      public String getName() {
         return name;
      }

      public CategoryType getType() {
         return type;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;

         Category category = (Category) o;

         if (!name.equals(category.name)) return false;
         return type == category.type;

      }

      @Override
      public int hashCode() {
         int result = name.hashCode();
         result = 31 * result + type.hashCode();
         return result;
      }
   }

   public synchronized void addEvent(Category category, Event e) {
      List<Event> cat = events.get(category);
      if (cat == null) {
         cat = new ArrayList<Event>();
         events.put(category, cat);
      }
      cat.add(e);
      updateTimestamps(e);
   }

   private void updateTimestamps(Event e) {
      firstTimestamp = Math.min(firstTimestamp, e.getStarted());
      lastTimestamp = Math.max(lastTimestamp, e.getEnded());
   }

   public synchronized Set<Category> getEventCategories() {
      return events.keySet();
   }

   public synchronized List<Event> getEvents(Category category) {
      return events.get(category);
   }

   public long getFirstTimestamp() {
      return firstTimestamp;
   }

   public long getLastTimestamp() {
      return lastTimestamp;
   }

   @Override
   public int compareTo(Timeline o) {
      return Integer.compare(slaveIndex, o.slaveIndex);
   }

   /**
    * Generic event in timeline
    */
   public abstract static class Event implements Serializable, Comparable<Event> {
      public final long timestamp;

      protected Event(long timestamp) {
         this.timestamp = timestamp;
      }

      protected Event() {
         this(TimeService.currentTimeMillis());
      }

      @Override
      public int compareTo(Event o) {
         return Long.compare(timestamp, o.timestamp);
      }

      public long getStarted() {
         return timestamp;
      }

      public long getEnded() {
         return timestamp;
      }
   }

   /**
    * Event that presents a sequence of changing values, such as CPU utilization
    */
   public static class ValueEvent extends Event {
      public final Number value;

      public ValueEvent(long timestamp, Number value) {
         super(timestamp);
         this.value = value;
      }

      public ValueEvent(Number value) {
         this.value = value;
      }

      @Override
      public String toString() {
         // doubles require %f, integers %d -> we use %s
         return String.format("Value{timestamp=%d, value=%s}", timestamp, value);
      }
   }

   /**
    * Occurence of this event is not a value in any series, such as slave crash.
    */
   public static class TextEvent extends Event {
      public final String text;

      public TextEvent(long timestamp, String text) {
         super(timestamp);
         this.text = text;
      }

      public TextEvent(String text) {
         this.text = text;
      }

      @Override
      public String toString() {
         return String.format("TextEvent{timestamp=%d, text=%s}", timestamp, text);
      }
   }

   /**
    * Event representing some continuous operation taking place for some period of time
    */
   public static class IntervalEvent extends Event {
      public final String description;
      public final long duration; // milliseconds

      public IntervalEvent(long timestamp, String description, long duration) {
         super(timestamp);
         this.description = description;
         this.duration = duration;
      }

      public IntervalEvent(String description, long duration) {
         this.description = description;
         this.duration = duration;
      }

      @Override
      public long getEnded() {
         return timestamp + duration;
      }

      @Override
      public String toString() {
         return String.format("IntervalEvent{timestamp=%d, duration=%d, description=%s}",
            timestamp, duration, description);
      }
   }

   /**
    * Dummy class used for remote signalization
    */
   public static class Request implements Serializable {
   }
}
