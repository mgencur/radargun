package org.radargun.stages.infinispan;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.radargun.DistStageAck;
import org.radargun.config.Property;
import org.radargun.config.Stage;
import org.radargun.service.Infinispan60HotrodService;
import org.radargun.stages.AbstractDistStage;
import org.radargun.stages.lifecycle.LifecycleHelper;

@Stage(doc = "Stage to change the socket timeout for an Infinispan HotRod service")
public class ReconfigureInfinispanServiceSocketTimeoutStage extends AbstractDistStage {
   @Property(optional = false, doc = "New socket timeout value")
   public int newSocketTimeout;

   @Property(optional = false, doc = "New connection timeout value")
   public int newConnectionTimeout;

   @Property(optional = false, doc = "New maxRetries value")
   public int newMaxRetries;

   @Override
   public DistStageAck executeOnSlave() {
      DistStageAck result = new DistStageAck(slaveState);
      Object servinceInstance = slaveState.get(LifecycleHelper.SERVICE_INSTANCE);
      if (servinceInstance != null && servinceInstance instanceof Infinispan60HotrodService) {
         Infinispan60HotrodService instance = (Infinispan60HotrodService) servinceInstance;
         ConfigurationBuilder builder = new ConfigurationBuilder();
         builder.read(instance.getConfiguration());
         builder.socketTimeout(newSocketTimeout);
         builder.connectionTimeout(newConnectionTimeout);
         builder.maxRetries(newMaxRetries);
         log.info("Max retries reconfigured to :" + newMaxRetries);
         instance.setRemoteCacheManagerConfiguration(builder.build());
         instance.init();
      }
      return result;
   }

}
