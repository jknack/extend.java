/**
 * Copyright (c) 2013 Edgar Espina
 *
 * This file is part of amd4j (https://github.com/jknack/amd4j)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jknack.extend;

/**
 * Used for derived properties using the {@link Extend} methods.
 * <p>
 * Usage:
 * </p>
 *
 * <pre>
 * import static com.github.jknack.extend.Extend.*;
 *
 * ...
 *
 * MyObject extended =
 *   extend(object,
 *     $("propertyA", new Function<MyObject, Integer>() {
 *       public Integer apply(MyObject object) {
 *         return ...;
 *       }
 *     })
 *   );
 *
 * </pre>
 *
 * @author edgar.espina
 * @since 0.1.0
 * @param <In> The input object.
 * @param <Out> The derived value.
 */
public interface Function<In, Out> {

  /**
   * <p>
   * Usage:
   * </p>
   *
   * <pre>
 * import static com.github.jknack.extend.Extend.*;
 *
 * ...
 *
 * MyObject extended =
 *   extend(object,
 *     $("propertyA", new Function<MyObject, Integer>() {
 *       public Integer apply(MyObject object) {
 *         return ...;
 *       }
 *     })
 *   );
 *
 * </pre>
   *
   * @param value The input object.
   * @return A derived value.
   */
  Out apply(In value);
}
