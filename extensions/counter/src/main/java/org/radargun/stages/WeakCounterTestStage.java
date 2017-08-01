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
import org.radargun.traits.InjectTrait;
import org.radargun.traits.WeakCounterOperations;

/**
 * @author Martin Gencur
 */
@Namespace(name = WeakCounterTestStage.NAMESPACE)
@Stage(doc = "Tests a weak clustered counter")
public class WeakCounterTestStage extends TestStage {

   public static final String NAMESPACE = "urn:radargun:stages:counter:" + Version.SCHEMA_VERSION;

   @Property(doc = "Counter name.", optional = false)
   protected String counterName;

   @Property(doc = "Delta to add for ADD operation. Default is 1.")
   protected int delta = 1;

   @InjectTrait
   protected WeakCounterOperations counterOperations;

   @Override
   protected OperationSelector createOperationSelector() {
      return new RatioOperationSelector.Builder().add(WeakCounterOperations.ADD, 1).build();
   }

   @Override
   public OperationLogic getLogic() {
      return new WeakCounterLogic();
   }

   /**
    * Stressor logic for weak counter operations. This logic is only for performance testing.
    * Consistency of weak counters is not verified.
    */
   protected class WeakCounterLogic extends OperationLogic {
      private WeakCounterOperations.WeakCounter counter;

      public WeakCounterLogic() {
      }

      @Override
      public void init(Stressor stressor) {
         super.init(stressor);
         this.counter = counterOperations.getWeakCounter(counterName);
         stressor.setUseTransactions(false);//transactions for counter do not make sense
      }

      @Override
      public void run(Operation operation) throws RequestException {
         Invocation<Void> invocation = new WeakCounterInvocations.Add(counter, delta);
         stressor.makeRequest(invocation);
      }
   }
}
