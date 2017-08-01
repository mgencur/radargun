package org.radargun.service;

import org.radargun.Service;
import org.radargun.traits.ProvidesTrait;
import org.radargun.traits.StrongCounterOperations;
import org.radargun.traits.WeakCounterOperations;

/**
 * @author Martin Gencur
 */
@Service(doc = InfinispanEmbeddedService.SERVICE_DESCRIPTION)
public class Infinispan91EmbeddedService extends Infinispan90EmbeddedService {

   @ProvidesTrait
   public StrongCounterOperations createStrongCounterOperations() {
      return new Infinispan91StrongCounterOperations(this);
   }

   @ProvidesTrait
   public WeakCounterOperations createWeakCounterOperations() {
      return new Infinispan91WeakCounterOperations(this);
   }
}