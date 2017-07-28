package org.radargun.traits;

import java.util.concurrent.CompletableFuture;
import org.radargun.Operation;

/**
 * @author Martin Gencur
 */
@Trait(doc = "Strong counter operations.")
public interface StrongCounterOperations {
   String TRAIT = StrongCounterOperations.class.getSimpleName();

   Operation INCREMENT_AND_GET = Operation.register(TRAIT + ".INCREMENT_AND_GET");
   Operation DECREMENT_AND_GET = Operation.register(TRAIT + ".DECREMENT_AND_GET");
   Operation ADD_AND_GET = Operation.register(TRAIT + ".ADD_AND_GET");

   StrongCounter getStrongCounter(String name);

   /**
    * Helper interface which mimics StrongCounter interface from Infinispan.
    */
   interface StrongCounter {
      /**
       * Atomically increments the counter and returns the new value.
       *
       * @return The new value.
       */
      CompletableFuture<Long> incrementAndGet();

      /**
       * Atomically decrements the counter and returns the new value
       *
       * @return The new value.
       */
      CompletableFuture<Long> decrementAndGet();

      /**
       * Atomically adds the given value and return the new value.
       *
       * @param delta The non-zero value to add. It can be negative.
       * @return The new value.
       */
      CompletableFuture<Long> addAndGet(long delta);
   }
}
