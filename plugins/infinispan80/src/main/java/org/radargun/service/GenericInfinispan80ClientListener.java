package org.radargun.service;

import org.infinispan.client.hotrod.annotation.*;
import org.infinispan.client.hotrod.event.ClientCacheEntryCreatedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryExpiredEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryModifiedEvent;
import org.infinispan.client.hotrod.event.ClientCacheEntryRemovedEvent;
import org.radargun.logging.Log;
import org.radargun.logging.LogFactory;
import org.radargun.traits.CacheListeners;

@ClientListener
public class GenericInfinispan80ClientListener extends InfinispanClientListeners.GenericClientListener {

    protected static final Log log = LogFactory.getLog(GenericInfinispan80ClientListener.class);

    @ClientCacheEntryExpired
    public void expired(ClientCacheEntryExpiredEvent e) {
        for (CacheListeners.ExpiredListener listener : expired) {
            try {
                listener.expired(e.getKey(), null);
            } catch (Exception ex) {
                log.error("Listener " + listener + " has thrown an exception", ex);
            }
        }
    }
}