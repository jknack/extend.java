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

import static org.apache.commons.lang3.Validate.notEmpty;
import static org.apache.commons.lang3.Validate.notNull;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import net.sf.cglib.beans.BeanGenerator;
import net.sf.cglib.beans.BeanMap;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * <p>
 * Extends JavaBean or convert them to {@link Map} and add new properties at runtime.
 * </p>
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
 *     $("propertyA", value),
 *     $("propertyB", value),
 *     ...
 *   );
 *
 * </pre>
 * <p>
 * Derived Properties:
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
 * <p>
 * Convert a JavaBean to a Map:
 * </p>
 *
 * <pre>
 * import static com.github.jknack.extend.Extend.*;
 *
 * ...
 *
 * Map<String, Object> extended =
 *   map(object,
 *     $("propertyA", value)
 *   );
 *
 * </pre>
 *
 * @author edgar.espina
 * @since 0.1.0
 */
public final class Extend {

  /**
   * The method interceptor.
   *
   * @author edgar.espina
   *
   */
  static class ExtendInterceptor implements MethodInterceptor {

    /**
     * The source object.
     */
    private Object source;

    /**
     * Extra properties.
     */
    private Map<String, Object> properties;

    /**
     * Internal use only.
     */
    private static final Object UNRESOLVED = new Object();

    /**
     * Creates a new {@link ExtendInterceptor}.
     *
     * @param source The object source.
     * @param properties The extra properties.
     */
    public ExtendInterceptor(final Object source, final Map<String, Object> properties) {
      this.source = source;
      this.properties = properties;
    }

    /**
     * Resolve a value using one of the provided resolvers.
     *
     * @param resolvers A resolver list.
     * @return A resolved value or null.
     * @throws Exception If a method invocation fails.
     */
    private Object resolve(final Callable<Object>... resolvers) throws Exception {
      for (Callable<Object> resolver : resolvers) {
        Object value = resolver.call();
        if (value != UNRESOLVED) {
          return value;
        }
      }
      return null;
    }

    /**
     * Creates a resolve around a method invocation.
     *
     * @param method The target method.
     * @param source The object source.
     * @param args The method arguments.
     * @return A new resolver.
     */
    private static Callable<Object> asMethod(final Method method, final Object source,
        final Object[] args) {
      return new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          return method.invoke(source, args);
        }
      };
    }

    /**
     * Creates a new resolver around the extra properties.
     *
     * @param methodName The method's name.
     * @param args The argument list.
     * @return A new resolver.
     */
    private Callable<Object> asMapEntry(final String methodName, final Object... args) {
      return new Callable<Object>() {
        @Override
        public Object call() throws Exception {
          String prefix = "get";
          if (methodName.startsWith(prefix)) {
            StringBuilder buffer = new StringBuilder(methodName.substring(prefix.length()));
            buffer.setCharAt(0, Character.toLowerCase(buffer.charAt(0)));
            String propertyName = buffer.toString();
            if (properties.containsKey(propertyName)) {
              return properties.get(propertyName);
            }
            return UNRESOLVED;
          }
          return UNRESOLVED;
        }
      };
    }

    @SuppressWarnings({"rawtypes", "unchecked" })
    @Override
    public Object intercept(final Object extended, final Method method, final Object[] args,
        final MethodProxy proxy) throws Throwable {
      String methodName = method.getName();
      if (methodName.startsWith("set")) {
        return null;
      }
      if ("hashCode".equals(methodName)) {
        return source.hashCode();
      }
      if ("equals".equals(methodName)) {
        return source.equals(args[0]);
      }
      Callable[] resolvers = {
          asMapEntry(methodName, args),
          asMethod(method, source, args)
      };

      Object value = resolve(resolvers);
      if (value instanceof Function) {
        value = ((Function) value).apply(source);
      }
      return value;
    }

  }

  /**
   * Helper class for simplify object property creation.
   * Use {@link Extend#$(String, Object)} factory method.
   *
   * @author edgar.espina
   * @since 0.1.0
   * @see Extend#$(String, Object)
   */
  public static final class Property {

    /**
     * The property's name. Required.
     */
    public final String name;

    /**
     * The property's value. Required.
     */
    public final Object value;

    /**
     * Creates a new {@link Property}.
     *
     * @param name The property's name. Required.
     * @param value The property's value. Required.
     */
    private Property(final String name, final Object value) {
      this.name = notEmpty(name, "The name is required.");
      this.value = notNull(value, "The value is required.");
    }

  }

  /**
   * Denied!
   */
  private Extend() {
  }

  /**
   * Factory method for {@link Property}.
   *
   * @param name The property's name. Required.
   * @param value The property's value. Required.
   * @return A new {@link Property}.
   */
  public static Property $(final String name, final Object value) {
    return new Property(name, value);
  }

  /**
   * Extends each object with the given properties.
   *
   * @param sources The objects to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A list of extended and immutable objects.
   */
  public static <T> List<T> extend(final Iterable<T> sources, final Property... properties) {
    notNull(sources, "The source object is required.");
    notEmpty(properties, "The properties is required");
    List<T> result = new ArrayList<T>();
    for (T source : sources) {
      result.add(extend(source, properties));
    }
    return result;
  }

  /**
   * Extends an object with the given properties.
   *
   * @param source The object to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new extended and immutable object.
   */
  public static <T> T extend(final T source, final Property... properties) {
    notNull(source, "The source object is required.");
    notEmpty(properties, "The properties is required");
    return extend(source, toMap(properties));
  }

  /**
   * Extends each object with the given properties.
   *
   * @param sources The objects to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A list of extended and immutable objects.
   */
  public static <T> List<T> extend(final Iterable<T> sources,
      final Map<String, Object> properties) {
    notNull(sources, "The sources object is required.");
    notEmpty(properties, "The properties is required");
    List<T> result = new ArrayList<T>();
    for (T source : sources) {
      result.add(extend(source, properties));
    }
    return result;
  }

  /**
   * Extends an object with the given properties.
   *
   * @param source The object to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new extended and immutable object.
   */
  @SuppressWarnings("unchecked")
  public static <T> T extend(final T source, final Map<String, Object> properties) {
    notNull(source, "The source object is required.");
    notEmpty(properties, "The properties is required");
    final BeanGenerator beanGenerator = new BeanGenerator();
    beanGenerator.setSuperclass(source.getClass());

    for (Entry<String, Object> property : properties.entrySet()) {
      Object value = property.getValue();
      if (value != null) {
        Class<?> propertyType = value.getClass();
        if (value instanceof Function) {
          propertyType = Object.class;
        }
        beanGenerator.addProperty(property.getKey(), propertyType);
      }
    }

    Class<T> extendedClass = (Class<T>) beanGenerator.createClass();

    Enhancer enhancer = new Enhancer();
    enhancer.setSuperclass(extendedClass);
    enhancer.setCallback(new ExtendInterceptor(source, properties));
    return (T) enhancer.create();
  }

  /**
   * Converts an object to a {@link Map}. The resulting map is the union of the bean properties and
   * all the extra properties.
   *
   * @param source The object to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new immutable map.
   */
  public static <T> Map<String, Object> map(final T source, final Property... properties) {
    return map(source, toMap(properties));
  }

  /**
   * Converts each object to a {@link Map}. The resulting map is the union of the bean properties
   * and all the extra properties.
   *
   * @param sources The objects to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new immutable map.
   */
  public static <T> List<Map<String, Object>> map(final Iterable<T> sources,
      final Property... properties) {
    return map(sources, toMap(properties));
  }

  /**
   * Converts each object to a {@link Map}. The resulting map is the union of the bean properties
   * and all the extra properties.
   *
   * @param sources The objects to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new immutable map.
   */
  public static <T> List<Map<String, Object>> map(final Iterable<T> sources,
      final Map<String, Object> properties) {
    notNull(sources, "The sources object is required.");
    List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
    for (T source : sources) {
      result.add(map(source, properties));
    }
    return result;
  }

  /**
   * Converts an object to a {@link Map}. The resulting map is the union of the bean properties and
   * all the extra properties.
   *
   * @param source The object to extend. Required.
   * @param properties The extra property set. Required.
   * @param <T> The source type.
   * @return A new immutable map.
   */
  @SuppressWarnings("unchecked")
  public static <T> Map<String, Object> map(final T source, final Map<String, Object> properties) {
    notNull(source, "The source object is required.");
    notNull(properties, "The properties is required");
    final BeanMap beanMap = BeanMap.create(source);
    return new AbstractMap<String, Object>() {
      @Override
      public Object put(final String key, final Object value) {
        return null;
      }

      @Override
      public Object get(final Object key) {
        Object value = properties.get(key);
        return value == null ? beanMap.get(key) : value;
      }

      @Override
      public Set<Entry<String, Object>> entrySet() {
        LinkedHashMap<String, Object> mergedProperties = new LinkedHashMap<String, Object>(beanMap);
        mergedProperties.putAll(properties);
        return mergedProperties.entrySet();
      }
    };
  }

  /**
   * Convert a {@link Property array} to a {@link Map}.
   *
   * @param properties A property's array.
   * @return A new {@link Map}.
   */
  private static Map<String, Object> toMap(final Property... properties) {
    Map<String, Object> hash = new LinkedHashMap<String, Object>();
    for (Property property : properties) {
      hash.put(property.name, property.value);
    }
    return hash;
  }
}
