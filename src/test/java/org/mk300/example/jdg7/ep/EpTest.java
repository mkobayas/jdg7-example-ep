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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class EpTest {

    private static final Logger log = Logger.getLogger(EpTest.class);

    @Test
    public void test() throws Exception {

        try (JdgCluster cluster = new JdgCluster(3)) {

            EmbeddedCacheManager cm = cluster.getCacheManager();

            Cache<String, Ep<List<String>>> cache = cm.getCache("default");
            cache = cache.getAdvancedCache().withFlags(Flag.IGNORE_RETURN_VALUES);

            String key = "MyKey";
            
            // add "hoge"+i to the List 
            for(int i=1; i<=10; i++) {
                String add = "hoge" + i;
                EntryProcessor<List<String>> addEp = new EntryProcessor<>(
                        CopyOnWriteArrayList<String>::new, // initial value supplier, the value must be thread-safe or immutable.
                        (d) -> d.get().add(add));          // d.get() return current value, you can change value.
                                                           // if value is immutable, you can change value by d.set(newValue)

                log.debugf("addEp %s", add);
                cache.put(key, addEp);
            }
            
            // remove "hoge3" from the List
            String remove = "hoge3";
            EntryProcessor<List<String>> removeEp = new EntryProcessor<>(
                    CopyOnWriteArrayList<String>::new,
                    (d) -> d.get().remove(remove));

            log.debugf("removeEp %s", remove);
            cache.put(key, removeEp);
            
            
            // Get result value
            Ep<List<String>> result = cache.get(key);
            List<String> resultEntry = result.get();
            log.infof("end %s", resultEntry);
            // resultEntry = [hoge1, hoge2, hoge4, hoge5, hoge6, hoge7, hoge8, hoge9, hoge10]
            
            
            Assert.assertTrue(resultEntry.size() == 9);
            Assert.assertTrue(resultEntry.containsAll(Arrays.asList("hoge1", "hoge2", "hoge4", "hoge5", "hoge6",
                    "hoge7", "hoge8", "hoge9", "hoge10")));
            Assert.assertFalse(resultEntry.contains("hoge3"));
            
        }
    }
    
    
}
