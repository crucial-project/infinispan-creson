package org.infinispan.creson;

import org.infinispan.Cache;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Pierre Sutra
 * @since 7.0
 */
@org.testng.annotations.Test(testName = "BaseTest", groups = "unit")
public class BaseTest extends AbstractTest {

   private ConfigurationBuilder defaultConfigurationBuilder;
   private static List<Cache<Object,Object>> caches = new ArrayList<>();

   public BaseTest() {
      defaultConfigurationBuilder
            = getDefaultClusteredCacheConfig(CACHE_MODE, false);
      defaultConfigurationBuilder
            .clustering().hash().numOwners(getReplicationFactor())
            .locking().useLockStriping(false);
      defaultConfigurationBuilder.clustering().stateTransfer()
            .awaitInitialTransfer(true)
            .timeout(1000000)
            .fetchInMemoryState(true);
      if (MAX_ENTRIES!=Integer.MAX_VALUE)
         defaultConfigurationBuilder.eviction().maxEntries(MAX_ENTRIES);
   }

   @Override 
   public BasicCacheContainer container(int i) {
      return manager(i);
   }

   @Override 
   public Collection<BasicCacheContainer> containers() {
      return (Collection)getCacheManagers();
   }

   @Override
   public synchronized boolean addContainer() {
      EmbeddedCacheManager cm = addClusterEnabledCacheManager(defaultConfigurationBuilder);
      caches.add(cm.getCache());
      waitForClusterToForm();
      System.out.println("Node " + cm+ " added.");
      return true;
   }

   @Override
   public synchronized boolean deleteContainer() {
      assert caches.size()==containers().size();
      if (caches.size()==0)
         return false;
      BasicCacheContainer container = container(containers().size() - 1);
      container.stop();
      containers().remove(container);
      caches.remove(caches.size()-1);
      waitForClusterToForm();
      System.out.println("Node " + container+ " deleted.");
      return true;
   }

   @Override
   protected void createCacheManagers() throws Throwable {
      for(int i=0; i<getNumberOfManagers(); i++) {
         addContainer();
      }
      Factory.forCache(cache(0));
   }
}
