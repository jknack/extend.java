extend.java
======

Inspired by JavaScript, extend.java is a small library for augmenting a JavaBean instance at runtime.

usage
======

**Extends a JavaBean**

```java
import static com.github.jknack.extend.Extend.*;

...

MyObject extended =
  extend(object,
    $("propertyA", value),
    $("propertyB", value),
    ...
  );
```

**Derived Properties**

```java
import static com.github.jknack.extend.Extend.*;

...

MyObject extended =
  extend(object,
    $("propertyA", new Function<MyObject, Integer>() {
      public Integer apply(MyObject object) {
        return ...;
      }
    })
  );
```

**Convert a JavaBean to a Map**

```java
import static com.github.jknack.extend.Extend.*;

...

Map<String, Object> extended =
  map(object,
    $("propertyA", value)
  );
```

Generated objects (beans and maps) are **immutable**, so they can't be modified after creation.

why?
======
 * It removes some odd practices/techniques like [DTO](http://en.wikipedia.org/wiki/Data_transfer_object) creation or similar patterns.
 * If you expose Domain Object from an API you can easily augment the response per use case, security rules, etc.
 * Beside you can't access to dynamically created properties: MVC Frameworks, Template Engines, Serializers can!! because they use reflection.

dependencies
======

```
+- cglib:cglib-nodep:jar:2.2.2
+- org.apache.commons:commons-lang3:jar:3.1
```

help and support
======
 [Bugs, Issues and Features](https://github.com/jknack/amd4j/issues)

author
======
 [@edgarespina](https://twitter.com/edgarespina)

license
======
[Apache License 2](http://www.apache.org/licenses/LICENSE-2.0.html)
