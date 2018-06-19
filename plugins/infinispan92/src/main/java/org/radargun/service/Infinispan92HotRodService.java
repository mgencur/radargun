package org.radargun.service;

import org.radargun.Service;
import org.radargun.traits.ProvidesTrait;

/**
 * @author mgencur
 */
@Service(doc = Infinispan60HotrodService.SERVICE_DESCRIPTION)
public class Infinispan92HotRodService extends Infinispan90HotrodService {

   @ProvidesTrait
   public Infinispan92HotRodOperations createInfinispan92HotRodOperations() {
      return new Infinispan92HotRodOperations(this);
   }

   @ProvidesTrait
   public HotRodOperations createOperations() {
      return new Infinispan90HotRodOperations(this);
   }

}
