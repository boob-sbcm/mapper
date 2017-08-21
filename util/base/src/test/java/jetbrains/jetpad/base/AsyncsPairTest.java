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
package jetbrains.jetpad.base;

import jetbrains.jetpad.base.function.Consumer;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class AsyncsPairTest extends BaseTestCase {
  private SimpleAsync<Integer> first = new SimpleAsync<>();
  private SimpleAsync<String> second = new SimpleAsync<>();

  private Async<Pair<Integer, String>> pair;
  private Pair<Integer, String> result;
  private Throwable error;

  private void initPair(Async<Integer> first, Async<String> second) {
    pair = Asyncs.pair(first, second);
    pair.onResult(
        new Consumer<Pair<Integer, String>>() {
          @Override
          public void accept(Pair<Integer, String> item) {
            result = item;
          }
        },
        new Consumer<Throwable>() {
          @Override
          public void accept(Throwable item) {
            error = item;
          }
        });
  }

  @Test
  public void successFirstEarlier() {
    initPair(first, second);
    first.success(1);

    assertNull(result);

    second.success("a");

    assertSucceeded();
  }

  @Test
  public void successSecondEarlier() {
    initPair(first, second);
    second.success("a");

    assertNull(result);

    first.success(1);

    assertSucceeded();
  }

  @Test
  public void successWithNulls() {
    initPair(first, second);
    first.success(null);
    second.success(null);

    assertNotNull(result);
    assertNull(result.first);
    assertNull(result.second);
  }

  @Test
  public void successFirstThenFailure() {
    initPair(first, second);
    first.success(1);
    Throwable throwable = new Throwable();
    second.failure(throwable);

    assertSame(throwable, error);
  }

  @Test
  public void successSecondThenFailure() {
    initPair(first, second);
    second.success("a");
    Throwable throwable = new Throwable();
    first.failure(throwable);

    assertSame(throwable, error);
  }

  @Test
  public void failureFirstThenSuccessSecond() {
    initPair(first, second);
    first.failure(new Throwable());
    second.success("a");

    assertNotNull(error);
  }

  @Test
  public void failureSecondThenSuccessFirst() {
    initPair(first, second);
    second.failure(new Throwable());
    first.success(1);

    assertNotNull(error);
  }

  @Test
  public void doubleFailureFirstEarlier() {
    initPair(first, second);
    first.failure(new Throwable());
    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void doubleFailureSecondEarlier() {
    initPair(first, second);
    second.failure(new Throwable());
    first.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successThenFirstAlreadySucceeded() {
    initPair(Asyncs.constant(1), second);

    second.success("a");

    assertSucceeded();
  }

  @Test
  public void failureThenFirstAlreadySucceeded() {
    initPair(Asyncs.constant(1), second);

    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successThenSecondAlreadySucceeded() {
    initPair(first, Asyncs.constant("a"));

    first.success(1);

    assertSucceeded();
  }

  @Test
  public void failureThenSecondAlreadySucceeded() {
    initPair(first, Asyncs.constant("a"));

    first.failure(new Throwable());

    assertNotNull(error);
  }


  @Test
  public void successSecondThenFirstAlreadyFailed() {
    initPair(Asyncs.<Integer>failure(new Throwable()), second);

    second.success("a");

    assertNotNull(error);
  }

  @Test
  public void failureSecondThenFirstAlreadyFailed() {
    initPair(Asyncs.<Integer>failure(new Throwable()), second);

    second.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void successFirstThenSecondAlreadyFailed() {
    initPair(first, Asyncs.<String>failure(new Throwable()));

    first.success(1);

    assertNotNull(error);
  }

  @Test
  public void failureFirstThenSecondAlreadyFailed() {
    initPair(first, Asyncs.<String>failure(new Throwable()));

    first.failure(new Throwable());

    assertNotNull(error);
  }

  @Test
  public void bothSucceeded() {
    initPair(Asyncs.constant(1), Asyncs.constant("a"));

    assertSucceeded();
  }

  @Test
  public void firstSucceededSecondFailed() {
    initPair(Asyncs.constant(1), Asyncs.<String>failure(new Throwable()));

    assertNotNull(error);
  }

  @Test
  public void firstFailedSecondSucceeded() {
    initPair(Asyncs.<Integer>failure(new Throwable()), Asyncs.constant("a"));

    assertNotNull(error);
  }

  @Test
  public void bothFailed() {
    initPair(Asyncs.<Integer>failure(new Throwable()), Asyncs.<String>failure(new Throwable()));

    assertNotNull(error);
  }

  private void assertSucceeded() {
    assertNotNull(result);
    assertEquals(new Integer(1), result.first);
    assertEquals("a", result.second);
  }
}