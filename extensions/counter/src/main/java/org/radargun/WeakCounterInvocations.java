package org.radargun;

import org.radargun.stages.test.Invocation;
import org.radargun.traits.WeakCounterOperations;

public class WeakCounterInvocations {

   public static final class Add implements Invocation<Void> {
      private final WeakCounterOperations.WeakCounter counter;
      private final long delta;

      public Add(WeakCounterOperations.WeakCounter counter, long delta) {
         this.counter = counter;
         this.delta = delta;
      }

      @Override
      public Void invoke() {
         try {
            counter.add(delta);
            return null;
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

   public static final class AddGetValue implements Invocation<Long> {
      private final WeakCounterOperations.WeakCounter counter;
      private final long delta;

      public AddGetValue(WeakCounterOperations.WeakCounter counter, long delta) {
         this.counter = counter;
         this.delta = delta;
      }

      @Override
      public Long invoke() {
         try {
            counter.add(delta);
            return counter.getValue();
         } catch (Exception e) {
            throw new RuntimeException("Operation " +
               WeakCounterOperations.ADD_GET_VALUE.toString() + "failed", e);
         }
      }

      @Override
      public Operation operation() {
         return WeakCounterOperations.ADD_GET_VALUE;
      }

      @Override
      public Operation txOperation() {
         return WeakCounterOperations.ADD_GET_VALUE;
      }
   }
}
