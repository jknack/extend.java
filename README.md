extend.java
======

Extends a JavaBean instance at runtime (or convert them to ```Map```).

usage
======

** Extends a JavaBean**
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

** Derived Properties**

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

** Convert a JavaBean to a Map**

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

dependencies
======

```
+- cglib:cglib-nodep:jar:2.2.2
+- org.apache.commons:commons-lang3:jar:3.1
```

help and support
======
 [Bugs, Issues and Features](https://github.com/jknack/amd4j/issues)

related projects
======
 [r.js](http://requirejs.org/docs/optimization.html)

credits
======
 [@jrburke](https://github.com/jrburke)

author
======
 [@edgarespina](https://twitter.com/edgarespina)

license
======
[Apache License 2](http://www.apache.org/licenses/LICENSE-2.0.html)
