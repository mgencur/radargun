package org.radargun.traits;

import java.util.concurrent.TimeUnit;
import org.radargun.Operation;

/**
 * @author mgencur
 */
@Trait(doc = "Cache operations with a timeout.")
public interface TimeoutOperations {
   String TRAIT = TimeoutOperations.class.getSimpleName();
   Operation GET = Operation.register(TRAIT + ".GetWithTimeout");
   Operation PUT = Operation.register(TRAIT + ".PutWithTimeout");

   <K, V> Cache<K, V> getCache(String cacheName);

   interface Cache<K, V> {

      V get(K key, long timeout, TimeUnit timeUnit);

      V put(K key, V value, long timeout, TimeUnit timeUnit);
   }
}
