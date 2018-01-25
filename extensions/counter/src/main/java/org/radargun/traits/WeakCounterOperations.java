package org.radargun.traits;

import org.radargun.Operation;

@Trait(doc = "Counter operations.")
public interface WeakCounterOperations {
   String TRAIT = WeakCounterOperations.class.getSimpleName();

   Operation ADD = Operation.register(TRAIT + ".Add");
   Operation ADD_GET_VALUE = Operation.register(TRAIT + ".AddGetValue");

   WeakCounter getWeakCounter(String name);

   interface WeakCounter {
      void reset() throws Exception;

      long getValue() throws Exception;

      void add(long delta) throws Exception;
   }
}
