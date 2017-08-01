package org.radargun.traits;

import java.util.concurrent.CompletableFuture;
import org.radargun.Operation;

/**
 * @author Martin Gencur
 */
@Trait(doc = "Strong counter operations.")
public interface WeakCounterOperations {
   String TRAIT = WeakCounterOperations.class.getSimpleName();

   Operation ADD = Operation.register(TRAIT + ".ADD");

   WeakCounter getWeakCounter(String name);

   /**
    * Helper interface which mimics WeakCounter interface from Infinispan.
    */
   interface WeakCounter {
      /**
       * Adds the given value to the counter.
       *
       * @param delta The value to add. It can be negative.
       */
      CompletableFuture<Void> add(long delta);
   }
}
