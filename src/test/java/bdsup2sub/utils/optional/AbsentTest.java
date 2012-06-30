/*
 * Copyright 2012 Miklos Juhasz (mjuhasz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bdsup2sub.utils.optional;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class AbsentTest {

    private Optional<String> subject = Optional.absent();

    @Test
    public void shouldNotBePresent() throws Exception {
        assertFalse(subject.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldGetBeIllegal() throws Exception {
        subject.get();
    }

    @Test
    public void shouldReturnNullForValue() throws Exception {
        assertNull(subject.orNull());
    }
}
