package org.radargun.stages.test;

import java.util.concurrent.TimeUnit;

import org.radargun.utils.TimeService;
import org.radargun.utils.Utils;

/**
 * Time-based limitation of the stress test.
 *
 * @author Mircea Markus &lt;Mircea.Markus@jboss.com&gt;
 */
public class TimeStressorCompletion extends AbstractCompletion {

   protected static final String PROGRESS_STRING = "Number of operations executed by this thread: %d. Elapsed time: %s. Remaining: %s. Total: %s.";
   private volatile long lastPrint = -1;
   private final long duration;
   private final long logFrequency = TimeUnit.SECONDS.toNanos(20);

   /**
    * @param duration Duration of the test in nanoseconds. When duration is 0 the test will run indefinitely.
    */
   public TimeStressorCompletion(long duration) {
      this.duration = TimeUnit.MILLISECONDS.toNanos(duration);
   }

   @Override
   public boolean moreToRun() {
      boolean moreToRun = duration == 0 ? true : TimeService.nanoTime() < startTime + duration;
      if (!moreToRun) {
         runCompletionHandler();
      }
      return moreToRun;
   }

   @Override
   public void logProgress(int executedOps) {
      long now = TimeService.nanoTime();
      //make sure this info is not printed more frequently than 20 secs
      if (lastPrint < 0 || (now - lastPrint) < logFrequency) {
         return;
      }
      synchronized (this) {
         if (now - lastPrint < logFrequency) return;
         lastPrint = now;
         //make sure negative durations are not printed
         long remaining = Math.max(0, (startTime + duration) - now);
         log.infof(PROGRESS_STRING, executedOps, Utils.getNanosDurationString(now - startTime),
            Utils.getNanosDurationString(remaining), Utils.getNanosDurationString(duration));
      }
   }
}
