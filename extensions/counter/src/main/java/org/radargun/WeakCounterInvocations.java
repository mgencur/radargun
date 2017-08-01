package org.radargun;

import org.radargun.stages.test.Invocation;
import org.radargun.traits.WeakCounterOperations;


/**
 * Provides {@link Invocation} implementations for operations from traits
 * {@link WeakCounterOperations}
 *
 * @author Martin Gencur
 */
public class WeakCounterInvocations {
   public static final class Add implements Invocation<Void> {
      private final WeakCounterOperations.WeakCounter weakCounter;
      private final long delta;

      public Add(WeakCounterOperations.WeakCounter strongCounter, long delta) {
         this.weakCounter = strongCounter;
         this.delta = delta;
      }

      @Override
      public Void invoke() {
         try {
            return weakCounter.add(delta).get();
         } catch (Exception e) {
            throw new RuntimeException("Operation " +
               WeakCounterOperations.ADD.toString() + "failed", e);
         }
      }

      @Override
      public Operation operation() {
         return WeakCounterOperations.ADD;
      }

      @Override
      public Operation txOperation() {
         return WeakCounterOperations.ADD;
      }
   }
}
