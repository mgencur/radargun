package org.radargun.cachewrappers;

import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.config.Config;
import com.hazelcast.core.Transaction;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.radargun.CacheWrapper;
import org.radargun.utils.TypedProperties;

import java.io.InputStream;
import java.util.Map;

/**
 *
 */
public class HazelcastWrapper implements CacheWrapper {

   protected final Log log = LogFactory.getLog(getClass());
   private final boolean trace = log.isTraceEnabled();

   private static final String DEFAULT_MAP_NAME = "default";
   protected HazelcastInstance hazelcastInstance;
   protected Transaction tx;
   protected Map<Object, Object> hazelcastMap;

   @Override
   public void setUp(String config, boolean isLocal, int nodeIndex, TypedProperties confAttributes) throws Exception {
      log.info("Creating cache with the following configuration: " + config);
      String mapName = getMapName(confAttributes);
      InputStream configStream = getAsInputStreamFromClassLoader(config);
      Config cfg = new XmlConfigBuilder(configStream).build();
      hazelcastInstance = Hazelcast.newHazelcastInstance(cfg);
      hazelcastMap = hazelcastInstance.getMap(mapName);
      tx = hazelcastInstance.getTransaction();
   }

   protected String getMapName(TypedProperties confAttributes) {
      return confAttributes.containsKey("map") ? confAttributes.getProperty("map") : DEFAULT_MAP_NAME;
   }

   @Override
   public void tearDown() throws Exception {
      Hazelcast.shutdown();
   }

   @Override
   public boolean isRunning() {
      return true;
   }

   @Override
   public void put(String bucket, Object key, Object value) throws Exception {
      if (trace) log.trace("PUT key=" + key);
      hazelcastMap.put(key, value);
   }

   @Override
   public Object get(String bucket, Object key) throws Exception {
      if (trace) log.trace("GET key=" + key);
      return hazelcastMap.get(key);
   }

   @Override
   public Object remove(String bucket, Object key) throws Exception {
      if (trace) log.trace("REMOVE key=" + key);
      hazelcastMap.remove(key);
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public void empty() throws Exception {
      hazelcastMap.clear();
      //To change body of implemented methods use File | Settings | File Templates.
   }

   @Override
   public int getNumMembers() {
      return Hazelcast.getAllHazelcastInstances().size();
   }

   @Override
   public String getInfo() {
      return "There are " + hazelcastMap.size() + " entries in the cache.";
   }

   @Override
   public Object getReplicatedData(String bucket, String key) throws Exception {
      return get(bucket, key);
   }

   @Override
   public void startTransaction() {
      try {
         tx.begin();
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public void endTransaction(boolean successful) {
      try {
         if (successful) {
            tx.commit();
         } else {
            tx.rollback();
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public int getLocalSize() {
      return hazelcastMap.size();
   }

   @Override
   public int getTotalSize() {
      return 0;
   }

   private InputStream getAsInputStreamFromClassLoader(String filename) {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      InputStream is;
      try {
         is = cl == null ? null : cl.getResourceAsStream(filename);
      } catch (RuntimeException re) {
         // could be valid; see ISPN-827
         is = null;
      }
      if (is == null) {
         try {
            // check system class loader
            is = getClass().getClassLoader().getResourceAsStream(filename);
         } catch (RuntimeException re) {
            // could be valid; see ISPN-827
            is = null;
         }
      }
      return is;
   }
}
