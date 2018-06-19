package org.radargun.service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.commons.CacheException;
import org.radargun.traits.TimeoutOperations;

/**
 * @author mgencur
 */
public class Infinispan92HotRodOperations implements TimeoutOperations {

   protected final InfinispanHotrodService service;

   public Infinispan92HotRodOperations(InfinispanHotrodService service) {
      this.service = service;
   }

   public <K, V> TimeoutOperations.Cache<K, V> getCache(String cacheName, long timeout, TimeUnit timeUnit) {
      if (cacheName == null) {
         cacheName = service.getCacheName();
      }
      if (cacheName == null) {
         return new Infinispan92HotRodTimeoutOperationsCache<>((RemoteCache<K,V>) service.getManagerForceReturn().getCache(true));
      } else {
         return new Infinispan92HotRodTimeoutOperationsCache<>((RemoteCache<K,V>) service.getManagerForceReturn().getCache(cacheName, true));
      }
   }

   protected class Infinispan92HotRodTimeoutOperationsCache<K, V> implements TimeoutOperations.Cache<K, V> {
      private RemoteCache<K, V> cache;

      public Infinispan92HotRodTimeoutOperationsCache(RemoteCache<K, V> cache) {
         this.cache = cache;
      }

      @Override
      public V get(K key, long timeout, TimeUnit timeUnit) {
         try {
            return cache.getAsync(key).get(timeout, timeUnit);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CacheException(e);
         } catch (ExecutionException e) {
            throw new CacheException(e);
         } catch (TimeoutException e) {
            throw new CacheException(e);
         }
      }

      @Override
      public V put(K key, V value, long timeout, TimeUnit timeUnit) {
         try {
            return cache.putAsync(key, value).get(timeout, timeUnit);
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CacheException(e);
         } catch (ExecutionException e) {
            throw new CacheException(e);
         } catch (TimeoutException e) {
            throw new CacheException(e);
         }
      }
   }

}
