package com.github.jknack.extend;

import static com.github.jknack.extend.Extend.$;
import static com.github.jknack.extend.Extend.extend;
import static com.github.jknack.extend.Extend.map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class BeanExtendTest {

  public static class Person {
    private String name;

    private boolean fun = true;

    public Person(final String name) {
      this.name = name;
    }

    public Person() {
    }

    public String getName() {
      return name;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public boolean isFun() {
      return fun;
    }

    public void setFun(final boolean fun) {
      this.fun = fun;
    }
  }

  @Test
  public void bean() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("age", 50),
            $("fun", false)
        );
    assertEquals("moe", moe.getName());
    assertEquals(false, moe.isFun());
    assertEquals(50, get("getAge", moe));
  }

  @Test
  public void immutableBean() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("age", 50)
        );
    // the next two calls should be ignored.
    moe.setName("curly");
    moe.setFun(false);
    set("setAge", moe, 45);
    // no change should be made.
    assertEquals("moe", moe.getName());
    assertEquals(true, moe.isFun());
    assertEquals(50, get("getAge", moe));
  }

  @Test
  public void extendAllTest() throws Exception {
    String[] names = {"moe", "larry", "curly" };
    List<Person> stooges =
        extend(
            Arrays.asList(
                new Person(names[0]),
                new Person(names[1]),
                new Person(names[2])
                ),
            $("age", 45)
        );
    assertEquals(3, stooges.size());
    for (int i = 0; i < names.length; i++) {
      assertEquals(names[i], stooges.get(i).getName());
      assertEquals(45, get("getAge", stooges.get(i)));
    }
  }

  @Test
  public void boolProperty() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("bool", true)
        );
    assertEquals("moe", get("getName", moe));
    assertEquals(true, get("getBool", moe));
  }

  @Test
  public void functionProperty() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("age", 50),
            $("crazy", new Function<Person, String>() {
              @Override
              public String apply(final Person value) {
                return "crazy's " + value.getName();
              }
            })
        );
    assertEquals("moe", get("getName", moe));
    assertEquals(50, get("getAge", moe));
    assertEquals("crazy's moe", get("getCrazy", moe));
  }

  @Test
  public void overrideProperty() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("name", "Moe Howard")
        );
    assertEquals("Moe Howard", moe.getName());
  }

  @Test
  public void overridePropertyUseSuper() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("name", new Function<Person, String>() {
              @Override
              public String apply(final Person value) {
                return "super " + value.getName();
              }
            })
        );
    assertEquals("super moe", moe.getName());
  }

  @Test
  public void classWithSameSetOfPropertyShouldHaveSameIdentity() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("age", 50)
        );

    Person curly =
        extend(new Person("curly"),
            $("age", 45)
        );
    assertEquals(moe.getClass(), curly.getClass());
  }

  @Test
  public void classWithDifferentSetOfPropertyShouldHaveDifferentIdentity() throws Exception {
    Person moe =
        extend(new Person("moe"),
            $("age", 50)
        );

    Person curly =
        extend(new Person("curly"),
            $("age", 50),
            $("middleName", "")
        );
    assertNotSame(moe.getClass(), curly.getClass());
  }

  @Test
  public void beanMap() throws Exception {
    Person moe = new Person("moe");
    Map<String, Object> map =
        map(moe,
            $("age", 50)
        );
    assertEquals("moe", map.get("name"));
    assertEquals(50, map.get("age"));
  }

  @Test
  public void immutableMap() throws Exception {
    Person moe = new Person("moe");
    Map<String, Object> map =
        map(moe,
            $("age", 50)
        );
    // change are ignored
    map.put("name", "curly");
    map.put("age", 45);
    // no change should be made
    assertEquals("moe", map.get("name"));
    assertEquals(50, map.get("age"));
  }

  private static Object get(final String methodName, final Object source)
      throws Exception {
    Method method = source.getClass().getDeclaredMethod(methodName);
    return method.invoke(source);
  }

  private static Object set(final String methodName, final Object source, final Object... args)
      throws Exception {
    Method method = source.getClass().getDeclaredMethod(methodName, args[0].getClass());
    return method.invoke(source, args);
  }
}
