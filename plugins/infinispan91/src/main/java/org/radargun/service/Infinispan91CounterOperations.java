package org.radargun.service;

import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.StrongCounter;
import org.radargun.traits.CounterOperations;
import org.radargun.traits.WeakCounterOperations;

/**
 * @author Martin Gencur
 */
public class Infinispan91CounterOperations implements CounterOperations, WeakCounterOperations {

   protected final InfinispanEmbeddedService service;

   public Infinispan91CounterOperations(InfinispanEmbeddedService service) {
      this.service = service;
   }

   @Override
   public Counter getCounter(String name) {
      CounterManager counterManager = EmbeddedCounterManagerFactory.asCounterManager(service.getCacheManager());
      return new CounterImpl(counterManager.getStrongCounter(name));
   }

   @Override
   public WeakCounter getWeakCounter(String name) {
      CounterManager counterManager = EmbeddedCounterManagerFactory.asCounterManager(service.getCacheManager());
      return new WeakCounterImpl(counterManager.getWeakCounter(name));
   }

   protected static class CounterImpl implements Counter {

      StrongCounter counter;

      public CounterImpl(StrongCounter counter) {
         this.counter = counter;
      }

      @Override
      public void reset() throws Exception {
         counter.reset().get();
      }

      @Override
      public long getValue() throws Exception {
         return counter.getValue().get();
      }

      @Override
      public long incrementAndGet() throws Exception {
         return counter.incrementAndGet().get();
      }

      @Override
      public long decrementAndGet() throws Exception {
         return counter.decrementAndGet().get();
      }

      @Override
      public long addAndGet(long delta)  throws Exception {
         return counter.addAndGet(delta).get();
      }

      @Override
      public boolean compareAndSet(long expect, long update) throws Exception {
         return counter.compareAndSet(expect, update).get();
      }
   }

   protected static class WeakCounterImpl implements WeakCounter {

      org.infinispan.counter.api.WeakCounter counter;

      public WeakCounterImpl(org.infinispan.counter.api.WeakCounter counter) {
         this.counter = counter;
      }

      @Override
      public void reset() throws Exception {
         counter.reset().get();
      }

      @Override
      public long getValue() throws Exception {
         return counter.getValue();
      }

      @Override
      public void add(long delta) throws Exception {
         counter.add(delta).get();
      }
   }
}
