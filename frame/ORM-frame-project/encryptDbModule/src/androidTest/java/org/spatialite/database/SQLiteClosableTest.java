/*
 * Copyright (C) 2009 The Android Open Source Project
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

package org.spatialite.database;

import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(JUnit4.class)
@SmallTest
public class SQLiteClosableTest {
    private class MockSQLiteClosable extends SQLiteClosable {
        private boolean mOnAllReferencesReleasedCalled = false;
        private boolean mOnAllReferencesReleasedFromContainerCalled = false;

        @Override
        protected void onAllReferencesReleased() {
            mOnAllReferencesReleasedCalled = true;
        }

        protected void onAllReferencesReleasedFromContainer() {
            mOnAllReferencesReleasedFromContainerCalled = true;
        }

        public boolean isOnAllReferencesReleasedCalled() {
            return mOnAllReferencesReleasedCalled;
        }

        public boolean isOnAllReferencesReleasedFromContainerCalled() {
            return mOnAllReferencesReleasedFromContainerCalled;
        }
    }

    @Test
    public void testAcquireReference() {
        MockSQLiteClosable closable = new MockSQLiteClosable();

        closable.acquireReference();
        closable.releaseReference();

        assertFalse(closable.isOnAllReferencesReleasedCalled());
        closable.releaseReference();
        // the reference count is 0 now.
        assertTrue(closable.isOnAllReferencesReleasedCalled());

        try {
            closable.acquireReference();
            fail("should throw IllegalStateException.");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void testReleaseReferenceFromContainer() {
        MockSQLiteClosable closable = new MockSQLiteClosable();

        closable.acquireReference();
        closable.releaseReferenceFromContainer();

        // the reference count is 1 now.
        assertFalse(closable.isOnAllReferencesReleasedFromContainerCalled());
        closable.releaseReferenceFromContainer();
        // the reference count is 0 now.
        assertTrue(closable.isOnAllReferencesReleasedFromContainerCalled());
    }
}
