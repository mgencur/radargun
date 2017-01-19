package org.radargun.reporting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.radargun.utils.TimeService;

/**
 * Events that should be presented in report
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class Timeline implements Serializable, Comparable<Timeline> {

   public final int slaveIndex;
   private Map<String, List<Event>> events = new HashMap<String, List<Event>>();
   private long firstTimestamp = Long.MAX_VALUE;
   private long lastTimestamp = Long.MIN_VALUE;

   public Timeline(int slaveIndex) {
      this.slaveIndex = slaveIndex;
   }

   public synchronized void addEvent(String category, Event e) {
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

   public synchronized Set<String> getEventCategories() {
      return events.keySet();
   }

   public synchronized List<Event> getEvents(String category) {
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

   public enum EventType {
      /* All events related to system resources (CPU, memory, network, etc.) */
      SYSMONITOR,
      /* Any other type of events, e.g. recording values in background stages */
      CUSTOM
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
