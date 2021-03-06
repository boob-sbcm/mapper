/*
 * Copyright 2012-2017 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertySelectionTest {
  C2 c2 = new C2();
  ReadableProperty<Integer> selProp = Properties.select(c2.ref, new Function<C1, ReadableProperty<Integer>>() {
    @Override
    public ReadableProperty<Integer> apply(C1 s) {
      return s.value;
    }
  }, 30);
  boolean changed = false;

  private void addListener() {
    selProp.addHandler(new EventHandler<PropertyChangeEvent<Integer>>() {
      @Override
      public void onEvent(PropertyChangeEvent<Integer> item) {
        changed = true;
      }
    });
  }

  @Test
  public void initialValue() {
    assertEquals(30, (int) selProp.get());
  }

  @Test
  public void valueSet() {
    addListener();
    C1 c1 = new C1(239);
    c2.ref.set(c1);
    assertEquals(239, (int) selProp.get());
    assertTrue(changed);
  }

  @Test
  public void subvalueChange() {
    addListener();
    C1 c1 = new C1(239);
    c2.ref.set(c1);

    changed = false;
    c1.value.set(30);
    assertTrue(changed);
  }

  @Test
  public void subvalueChangeListenerAddedAfterRefSet() {
    C1 c1 = new C1(239);
    c2.ref.set(c1);
    addListener();

    changed = false;
    c1.value.set(30);
    assertTrue(changed);
  }

  class C1 {
    ValueProperty<Integer> value = new ValueProperty<>();

    C1(Integer v) {
      value.set(v);
    }
  }

  class C2 {
    ValueProperty<C1> ref = new ValueProperty<>();
  }
}