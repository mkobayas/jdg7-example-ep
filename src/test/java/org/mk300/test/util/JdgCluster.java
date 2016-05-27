/*
 * Copyright 2016 Masazumi Kobayashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mk300.test.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.topology.ClusterTopologyManager;
import org.jboss.logging.Logger;

/**
 * 
 * @author mkobayas@redhat.com
 *
 */
public class JdgCluster implements AutoCloseable {

    private static final Logger log = Logger.getLogger(JdgCluster.class);
    
    protected List<EmbeddedCacheManager> cacheManagers;
    
    public JdgCluster(int clusterSize) {
        log.info("cluster starting");
        
        System.setProperty("jgroups.use.jdk_logger", "false"); // for fine jgroups logs
        System.setProperty("java.net.preferIPv4Stack", "true"); 

        System.setProperty("jgroups.join_timeout", "500");
        System.setProperty("jgroups.udp.ip_ttl", "0");
        
        cacheManagers = IntStream.rangeClosed(1, clusterSize).mapToObj(i -> {
            try {
                System.setProperty("node-name", "node"+i);
                EmbeddedCacheManager cm = new DefaultCacheManager("my-infinispan.xml", true);
                cm.getCacheNames().forEach(name -> {
                    cm.getCache(name).start();
                });
                log.infof("cm start %d", i);
                return cm;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }).collect(Collectors.toList());
        log.info("cluster started");
    }
    
    private void stop() {
        log.info("stopping");
        
        cacheManagers.get(0).getGlobalComponentRegistry().getComponent(ClusterTopologyManager.class).setRebalancingEnabled(false);
        Collections.reverse(cacheManagers); // avoid switching coordinator
        cacheManagers.forEach(cm -> {
            cm.stop();
        });
        
        log.info("Stoped");
    }
    
    public EmbeddedCacheManager getCacheManager() {
        return cacheManagers.get(0);
    }
    
    public EmbeddedCacheManager getCacheManager(int index) {
        return cacheManagers.get(index);
    }

    @Override
    public void close() throws Exception {
        stop();
    }
}
