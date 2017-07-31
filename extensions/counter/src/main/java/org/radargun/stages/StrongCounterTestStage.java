package org.radargun.stages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.radargun.DistStageAck;
import org.radargun.Operation;
import org.radargun.StageResult;
import org.radargun.StrongCounterInvocations;
import org.radargun.Version;
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
import org.radargun.state.SlaveState;
import org.radargun.stats.Statistics;
import org.radargun.traits.InjectTrait;
import org.radargun.traits.StrongCounterOperations;

/**
 * @author Martin Gencur
 */
@Namespace(name = StrongCounterTestStage.NAMESPACE)
@Stage(doc = "Tests a strong clustered counter")
public class StrongCounterTestStage extends TestStage {

   public static final String NAMESPACE = "urn:radargun:stages:counter:" + Version.SCHEMA_VERSION;

   @Property(doc = "Counter name.", optional = false)
   protected String counterName;

   @Property(doc = "Initial value of the counter expected by this stage. The test will start" +
      "counting from this value. Default is 0.")
   protected int initialValue = 0;

   @Property(doc = "Operation to test. Default is INCREMENT_AND_GET.")
   protected OperationName operationName = OperationName.INCREMENT_AND_GET;

   @Property(doc = "Delta to add for addAndGet operation. Default is 1.")
   protected int delta = 1;

   private Comparator<Long> comparator;

   enum OperationName {
      INCREMENT_AND_GET, DECREMENT_AND_GET, ADD_AND_GET
   }

   @InjectTrait
   protected StrongCounterOperations counterOperations;

   @Init
   @Override
   public void init() {
      super.init();
      if (OperationName.INCREMENT_AND_GET.equals(operationName)) {
         delta = 1;
      } else if (OperationName.DECREMENT_AND_GET.equals(operationName)) {
         delta = -1;
      }
      if (delta < 0) {
         comparator = Comparator.reverseOrder();
      } else {
         comparator = Comparator.naturalOrder();
      }
   }

   @Override
   protected OperationSelector createOperationSelector() {
      Operation op = Operation.getByName(StrongCounterOperations.TRAIT + "." + operationName.toString());
      return new RatioOperationSelector.Builder().add(op, 1).build();
   }

   @Override
   public OperationLogic getLogic() {
      return new StrongCounterLogic();
   }

   @Override
   protected DistStageAck newStatisticsAck(List<Stressor> stressors) {
      List<Long> data = new ArrayList<>();
      data.addAll(stressors.stream()
         .map(stressor -> ((StrongCounterLogic) stressor.getLogic()).valueSequence)
         .flatMap(values -> values.stream()) //merge values from all stressors into single list
         .collect(Collectors.toList()));
      return new ClusteredCounterAck(slaveState, gatherResults(stressors, new StatisticsResultRetriever()), data);
   }

   @Override
   public StageResult processAckOnMaster(List<DistStageAck> acks) {
      StageResult result = super.processAckOnMaster(acks);
      if (result.isError()) return result;

      List<ClusteredCounterAck> counterAcks = instancesOf(acks, ClusteredCounterAck.class);

      TreeSet<Long> allValues = getSortedValues(counterAcks);
      assertNoSkipped(allValues);

      return result;
   }

   private TreeSet<Long> getSortedValues(List<ClusteredCounterAck> counterAcks) {
      TreeSet<Long> allValues = new TreeSet<>(comparator);
      counterAcks.stream()
         .map(ack -> ack.values)
         .flatMap(values -> values.stream())
         .forEach(v -> {
            if (allValues.contains(v)) {
               throw new IllegalStateException("Inconsistent counter! The value " + v + " already returned by different thread!");
            } else {
               allValues.add(v);
            }
         });
      return allValues;
   }

   private void assertNoSkipped(TreeSet<Long> values) {
      long first = values.first();
      long previous = first;
      for (Long v : values.tailSet(first, false)) {
         long expected = previous + delta;
         if (v != expected) {
            throw new IllegalStateException("The value " + expected + " skipped by the counter! Current value: " + v);
         } else {
            previous = v;
         }
      }
   }

   /**
    * Stressor logic for strong counter operations. All operations
    * are supposed to use a shared counter between threads (this tests both consistency and
    * performance).
    */
   protected class StrongCounterLogic extends OperationLogic {
      private StrongCounterOperations.StrongCounter counter;
      private long previousValue;
      private List<Long> valueSequence;

      public StrongCounterLogic() {
         this.previousValue = initialValue;
         this.valueSequence = new ArrayList<>();
      }

      @Override
      public void init(Stressor stressor) {
         super.init(stressor);
         this.counter = counterOperations.getStrongCounter(counterName);
         stressor.setUseTransactions(false);//transactions for counter do not make sense
      }

      @Override
      public void run(Operation operation) throws RequestException {
         long currentValue;
         if (operation == StrongCounterOperations.INCREMENT_AND_GET) {
            Invocation<Long> invocation = new StrongCounterInvocations.IncrementAndGet(counter);
            currentValue = stressor.makeRequest(invocation);
            assertNotEqual(previousValue, currentValue);
         } else if (operation == StrongCounterOperations.DECREMENT_AND_GET) {
            Invocation<Long> invocation = new StrongCounterInvocations.DecrementAndGet(counter);
            currentValue = stressor.makeRequest(invocation);
            assertNotEqual(previousValue, currentValue);
         } else if (operation == StrongCounterOperations.ADD_AND_GET) {
            Invocation<Long> invocation = new StrongCounterInvocations.AddAndGet(counter, delta);
            currentValue = stressor.makeRequest(invocation);
            assertNotEqual(previousValue, currentValue);
         } else {
            throw new IllegalArgumentException(operation.name);
         }
         previousValue = currentValue; // exception was NOT thrown so we can update previous value for all operations
         valueSequence.add(currentValue); //record the value for eventual consistency check
      }

      private void assertNotEqual(long previous, long current) {
         if (previous == current) {
            throw new IllegalStateException("Inconsistent counter! The value should be different from " + previous);
         }
      }
   }

   protected static class ClusteredCounterAck extends StatisticsAck {
      List<Long> values;
      public ClusteredCounterAck(SlaveState slaveState, List<Statistics> statistics, List<Long> values) {
         super(slaveState, statistics, null);
         this.values = values;
      }
   }
}
