package org.radargun.stages;

import org.radargun.Operation;
import org.radargun.Version;
import org.radargun.WeakCounterInvocations;
import org.radargun.config.Namespace;
import org.radargun.config.Property;
import org.radargun.config.Stage;
import org.radargun.stages.test.Invocation;
import org.radargun.stages.test.OperationLogic;
import org.radargun.stages.test.OperationSelector;
import org.radargun.stages.test.RatioOperationSelector;
import org.radargun.stages.test.Stressor;
import org.radargun.stages.test.TestStage;
import org.radargun.traits.CounterOperations;
import org.radargun.traits.InjectTrait;
import org.radargun.traits.WeakCounterOperations;


/**
 * @author Martin Gencur
 */
@Namespace(name = WeakCounterTestStage.NAMESPACE)
@Stage(doc = "Tests a clustered/distributed counter")
public class WeakCounterTestStage extends TestStage {

   public static final String NAMESPACE = "urn:radargun:stages:counter:" + Version.SCHEMA_VERSION;

   @Property(doc = "Counter name.", optional = false)
   protected String counterName;

   @Property(doc = "Initial value of the counter expected by this stage. The test will start" +
      "counting from this value. Default is 0.")
   protected long initialValue = 0;

   @Property(doc = "Operation to test. Default is INCREMENT_AND_GET.")
   protected OperationName operationName = OperationName.ADD;

   @Property(doc = "Delta to add for addAndGet operation. Default is 1.")
   protected long delta = 1;

   enum OperationName {
      ADD, ADD_GET_VALUE
   }

   @InjectTrait
   protected WeakCounterOperations weakCounterOperations;

   @Override
   protected OperationSelector createOperationSelector() {
      switch (operationName) {
         case ADD:
            return new RatioOperationSelector.Builder().add(WeakCounterOperations.ADD, 1).build();
         case ADD_GET_VALUE:
            return new RatioOperationSelector.Builder().add(WeakCounterOperations.ADD_GET_VALUE, 1).build();
         default: throw new IllegalArgumentException("Unknown operation!");
      }
   }

   @Override
   public OperationLogic getLogic() {
      return new CounterLogic();
   }

   protected class CounterLogic extends OperationLogic {
      private WeakCounterOperations.WeakCounter weakCounter;
      private long previousValue;

      public CounterLogic() {
         this.previousValue = initialValue;
      }

      @Override
      public void init(Stressor stressor) {
         super.init(stressor);
         this.weakCounter = weakCounterOperations.getWeakCounter(counterName);
         try {
            this.weakCounter.reset();
         } catch (Exception e) {
            throw new RuntimeException("Failed to reset the counter!");
         }
         log.warn("Transactions ignored for Counter operations!");
         stressor.setUseTransactions(false);//transactions for counter do not make sense
      }

      @Override
      public void run(Operation operation) throws RequestException {
         if (operation == WeakCounterOperations.ADD) {
            Invocation<Void> invocation = new WeakCounterInvocations.Add(weakCounter, delta);
            stressor.makeRequest(invocation);
         } else if (operation == WeakCounterOperations.ADD_GET_VALUE) {
            Invocation<Long> invocation = new WeakCounterInvocations.AddGetValue(weakCounter, delta);
            stressor.makeRequest(invocation);
         } else {
            throw new IllegalArgumentException(operation.name);
         }
      }
   }
}
