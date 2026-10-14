/*
 * Copyright 2012-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.assertj.core.condition;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import org.assertj.core.api.Condition;

/// A [Condition] that matches values satisfying the assertions in a [Consumer].
///
/// This allows assertion helpers used with `satisfies` or `allSatisfy` to be reused
/// with condition-based assertions such as `haveExactly`.
/// A value matches if the consumer returns normally, and does not match if it
/// throws an [AssertionError].
/// Other exceptions and errors propagate to the caller.
///
/// Example:
/// ```java
/// Consumer<String> twoCharacters = value -> assertThat(value).hasSize(2);
/// Condition<String> condition = new SatisfyingCondition<>(twoCharacters);
/// condition.as("has two characters");
///
/// // succeeds: only "bb" satisfies the assertion
/// assertThat(List.of("a", "bb", "c")).haveExactly(1, condition);
/// // fails: two elements satisfy the assertion
/// assertThat(List.of("aa", "bb", "c")).haveExactly(1, condition);
/// ```
///
/// The description is stable across evaluations and can be customized with
/// [#as(String, Object...)].
/// It is not replaced by individual assertion failure messages.
/// The consumer must throw an assertion error to signal a mismatch; assertions
/// collected without throwing, for example by soft assertions, do not signal a mismatch.
/// Null values are passed to the consumer, which decides whether they match.
///
/// @param <T> the type of value this condition accepts.
/// @since 4.0.0
public final class SatisfyingCondition<T> extends Condition<T> {

  private final Consumer<? super T> assertions;

  /// Creates a condition adapting the given assertion consumer.
  ///
  /// @param assertions the consumer containing the assertions to evaluate.
  /// @throws NullPointerException if the consumer is null.
  public SatisfyingCondition(Consumer<? super T> assertions) {
    super("satisfies the assertions");
    this.assertions = requireNonNull(assertions, "The assertions consumer must not be null");
  }

  /// Evaluates the assertions for the given value.
  ///
  /// @param value the value passed to the assertion consumer.
  /// @return true if the consumer returns normally, or false if it throws an assertion error.
  @Override
  public boolean matches(T value) {
    try {
      assertions.accept(value);
      return true;
    } catch (AssertionError error) {
      return false;
    }
  }
}
