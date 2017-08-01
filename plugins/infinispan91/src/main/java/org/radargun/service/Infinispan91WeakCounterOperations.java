package org.radargun.service;

import java.util.concurrent.CompletableFuture;
import org.infinispan.counter.EmbeddedCounterManagerFactory;
import org.infinispan.counter.api.CounterManager;
import org.radargun.traits.WeakCounterOperations;

/**
 * @author Martin Gencur
 */
public class Infinispan91WeakCounterOperations implements WeakCounterOperations {

   protected final InfinispanEmbeddedService service;

   public Infinispan91WeakCounterOperations(InfinispanEmbeddedService service) {
      this.service = service;
   }

   @Override
   public WeakCounter getWeakCounter(String name) {
      CounterManager counterManager = EmbeddedCounterManagerFactory.asCounterManager(service.getCacheManager());
      return new WeakCounterImpl(counterManager.getWeakCounter(name));
   }

   protected static class WeakCounterImpl implements WeakCounter {

      org.infinispan.counter.api.WeakCounter counter;

      public WeakCounterImpl(org.infinispan.counter.api.WeakCounter counter) {
         this.counter = counter;
      }

      @Override
      public CompletableFuture<Void> add(long delta) {
         return counter.add(delta);
      }
   }
}
