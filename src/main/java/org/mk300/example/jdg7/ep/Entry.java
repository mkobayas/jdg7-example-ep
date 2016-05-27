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

import java.io.Serializable;

import org.infinispan.atomic.Delta;
import org.infinispan.atomic.DeltaAware;

/**
 * 
 * @author mkobayas@redhat.com
 *
 */
public class Entry<E> implements DeltaAware, Serializable, Ep<E> {
    private static final long serialVersionUID = 1L;
    
    private E value;

    @Override
    public E get() {
        return value;
    }

    public void set(E v) {
        value = v;
    }
    
    @Override
    public Delta delta() {
        return null;
    }

    @Override
    public void commit() {
    }

}
