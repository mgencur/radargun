package org.radargun.stages.cache.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.radargun.Operation;
import org.radargun.config.Init;
import org.radargun.config.Namespace;
import org.radargun.config.Property;
import org.radargun.config.Stage;
import org.radargun.stages.test.Invocation;
import org.radargun.stages.test.OperationLogic;
import org.radargun.stages.test.OperationSelector;
import org.radargun.stages.test.RatioOperationSelector;
import org.radargun.stages.test.Stressor;
import org.radargun.stages.test.TestStage;
import org.radargun.traits.InjectTrait;
import org.radargun.traits.TimeoutOperations;
import org.radargun.utils.NanoTimeConverter;

/**
 * @author mgencur
 */
@Namespace(name = TestStage.NAMESPACE, deprecatedName = TestStage.DEPRECATED_NAMESPACE)
@Stage(doc = "Test using TimeoutOperations")
public class TimeoutOperationsTestStage extends CacheOperationsTestStage {

   @Property(doc = "Ratio of GET requests. Default is 4.")
   protected int getRatio = 4;

   @Property(doc = "Ratio of PUT requests. Default is 1.")
   protected int putRatio = 1;

   @Property(doc = "Timeout value. Default is 500ms.", converter = NanoTimeConverter.class)
   protected Long operationTimeout = TimeUnit.MILLISECONDS.toNanos(500);

   @InjectTrait
   protected TimeoutOperations timeoutOperations;

   @Init
   @Override
   public void init() {
      super.init();
      statisticsPrototype.registerOperationsGroup(TimeoutOperations.class.getSimpleName() + ".Total",
         new HashSet<>(Arrays.asList(
            TimeoutOperations.GET,
            TimeoutOperations.PUT)));
   }

   @Override
   protected OperationSelector createOperationSelector() {
      RatioOperationSelector operationSelector = new RatioOperationSelector.Builder()
         .add(TimeoutOperations.GET, getRatio)
         .add(TimeoutOperations.PUT, putRatio)
         .build();
      return operationSelector;
   }

   @Override
   public OperationLogic getLogic() {
      return new Logic();
   }

   protected class Logic extends OperationLogic {
      protected TimeoutOperations.Cache cache;
      protected KeySelector keySelector;

      @Override
      public void init(Stressor stressor) {
         super.init(stressor);
         String cacheName = cacheSelector.getCacheName(stressor.getGlobalThreadIndex());
         cache = timeoutOperations.getCache(cacheName);
         stressor.setUseTransactions(false);
         keySelector = getKeySelector(stressor);
      }

      @Override
      public void run(Operation operation) throws RequestException {
         Object key = keyGenerator.generateKey(keySelector.next());
         Random random = stressor.getRandom();

         Invocation invocation;
         if (operation == TimeoutOperations.GET) {
            invocation = new CacheInvocations.GetWithTimeout(cache, key, operationTimeout, TimeUnit.NANOSECONDS);
         } else if (operation == TimeoutOperations.PUT) {
            invocation = new CacheInvocations.PutWithTimeout(cache, key,
               valueGenerator.generateValue(key, entrySize.next(random), random),
               operationTimeout, TimeUnit.NANOSECONDS);
         } else throw new IllegalArgumentException(operation.name);
         stressor.makeRequest(invocation);
      }
   }
}
