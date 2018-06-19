package org.radargun.service;

import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.protostream.SerializationContext;
import org.radargun.Service;
import org.radargun.config.Property;
import org.radargun.traits.ProvidesTrait;
import org.radargun.traits.Queryable;

/**
 * @author Vojtech Juranek &lt;vjuranek@redhat.com&gt;
 */
@Service(doc = Infinispan60HotrodService.SERVICE_DESCRIPTION)
public class Infinispan70HotrodService extends Infinispan60HotrodService {

   @Property(doc = "Maximum retries. Default is 10.")
   protected int maxRetries = 10;

   public RemoteCacheManager getRemoteManager(boolean forceReturn) {
      return forceReturn ? managerForceReturn : managerNoReturn;
   }

   @Override
   protected ConfigurationBuilder getDefaultHotRodConfig() {
      ConfigurationBuilder builder = super.getDefaultHotRodConfig();
      builder.maxRetries(maxRetries);
      log.info("Max retries set to: " + maxRetries);
      return builder;
   }

   @ProvidesTrait
   public InfinispanClientListeners createListeners() {
      return new InfinispanClientListeners(this);
   }

   @ProvidesTrait
   public Queryable getQueryable() {
      return new Infinispan70HotrodQueryable(this);
   }

   @Override
   protected void registerMarshallers(SerializationContext context) {
      for (RegisteredClass rc : classes) {
         try {
            context.registerMarshaller(rc.getMarshaller());
         } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate marshaller for " + rc.clazz, e);
         }
      }
   }

}
