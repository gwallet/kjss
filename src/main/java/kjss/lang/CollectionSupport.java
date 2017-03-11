/*
 *    Copyright 2017 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package kjss.lang;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public final class CollectionSupport {
    public static boolean isNullOrEmpty(@Nullable Collection<?> nullableCollection) {
        return nullableCollection == null || nullableCollection.isEmpty();
    }
    public static boolean isNullOrEmpty(@Nullable Map<?, ?> nullableMap) {
        return nullableMap == null || nullableMap.isEmpty();
    }
    public static boolean isNullOrEmpty(@Nullable Object[] nullableArray) {
        return nullableArray == null || nullableArray.length == 0;
    }
    private CollectionSupport() {}
}
