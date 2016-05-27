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

package org.mk300.example.jdg7.ep;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.mk300.test.util.JdgCluster;

/**
 * 
 * @author mkobayas@redhat.com
 *
 */
public class EpTest2 {

    private static final Logger log = Logger.getLogger(EpTest2.class);

    /**
     * test of concurrent read/write
     * @throws Exception
     */
    @Test
    public void testConcurrent() throws Exception {

        try (JdgCluster cluster = new JdgCluster(3)) {

            EmbeddedCacheManager cm = cluster.getCacheManager();

            Cache<String, Ep<List<String>>> cache1 = cm.getCache("default");
            Cache<String, Ep<List<String>>> cache = cache1.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);

            String key = "MyKey";
            
            IntStream.rangeClosed(1, 10).parallel().forEach(k -> {
                // add "hoge"+i to the List 
                for(int i=1; i<=1000; i++) {
                    String add = "hoge" + i;
                    EntryProcessor<List<String>> addEp = new EntryProcessor<>(
                            CopyOnWriteArrayList<String>::new,
                            (d) -> d.get().add(add));
    
                    cache.put(key, addEp);
                    Ep<List<String>> result = cache.get(key);
                    log.infof("size %d", result.get().size());
                }
            });
            
            
            // get result value
            Ep<List<String>> result = cache.get(key);
            List<String> resultEntry = result.get();
            log.infof("end %d", resultEntry.size());
            
            Assert.assertTrue(resultEntry.size() == 10000);
            
            // dump processor
//            EntryProcessor<List<String>> dumpEp = new EntryProcessor<>(
//                    CopyOnWriteArrayList<String>::new,
//                    (d) -> System.out.println(d.get()));
//
//            log.debugf("dumpEp %s", dumpEp);
//            cache.put(key, dumpEp);
        }
    }
}
