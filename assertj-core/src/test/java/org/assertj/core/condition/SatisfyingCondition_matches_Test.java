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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.assertj.core.util.AssertionsUtil.expectAssertionError;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class SatisfyingCondition_matches_Test {

  @Test
  void should_match_when_all_assertions_pass() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> {
      assertThat(value).startsWith("a");
      assertThat(value).hasSize(2);
    });
    // WHEN
    boolean matches = condition.matches("ab");
    // THEN
    then(matches).isTrue();
  }

  @Test
  void should_not_match_when_an_assertion_fails() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).hasSize(2));
    // WHEN
    boolean matches = condition.matches("a");
    // THEN
    then(matches).isFalse();
  }

  @Test
  void should_not_match_when_a_later_assertion_fails() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> {
      assertThat(value).startsWith("a");
      assertThat(value).hasSize(2);
    });
    // WHEN
    boolean matches = condition.matches("abc");
    // THEN
    then(matches).isFalse();
  }

  @Test
  void should_not_match_custom_assertion_error_without_message() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> {
      throw new AssertionError();
    });
    // WHEN
    boolean matches = condition.matches("a");
    // THEN
    then(matches).isFalse();
  }

  @Test
  void should_propagate_runtime_exception() {
    // GIVEN
    var failure = new IllegalStateException("Invalid test setup");
    var condition = new SatisfyingCondition<String>(value -> {
      throw failure;
    });
    // WHEN/THEN
    thenThrownBy(() -> condition.matches("a")).isSameAs(failure);
  }

  @Test
  void should_propagate_other_errors() {
    // GIVEN
    var failure = new LinkageError("Unavailable class");
    var condition = new SatisfyingCondition<String>(value -> {
      throw failure;
    });
    // WHEN/THEN
    thenThrownBy(() -> condition.matches("a")).isSameAs(failure);
  }

  @Test
  void should_allow_consumer_to_match_null() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).isNull());
    // WHEN
    boolean matches = condition.matches(null);
    // THEN
    then(matches).isTrue();
  }

  @Test
  void should_allow_consumer_to_reject_null() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).isNotNull());
    // WHEN
    boolean matches = condition.matches(null);
    // THEN
    then(matches).isFalse();
  }

  @Test
  void should_accept_consumer_of_supertype() {
    // GIVEN
    Consumer<CharSequence> assertions = value -> assertThat(value).hasSize(2);
    SatisfyingCondition<String> condition = new SatisfyingCondition<>(assertions);
    // WHEN
    boolean matches = condition.matches("bb");
    // THEN
    then(matches).isTrue();
  }

  @Test
  void should_preserve_custom_description_across_evaluations() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).hasSize(2));
    condition.as("has %s characters", 2);
    // WHEN
    boolean firstMatch = condition.matches("a");
    boolean secondMatch = condition.matches("bb");
    boolean thirdMatch = condition.matches("ccc");
    // THEN
    then(firstMatch).isFalse();
    then(secondMatch).isTrue();
    then(thirdMatch).isFalse();
    then(condition.description().value()).isEqualTo("has 2 characters");
  }

  @Test
  void should_invoke_consumer_once_per_match() {
    // GIVEN
    var calls = new AtomicInteger();
    var condition = new SatisfyingCondition<String>(value -> calls.incrementAndGet());
    // WHEN
    boolean matches = condition.matches("a");
    // THEN
    then(matches).isTrue();
    then(calls).hasValue(1);
  }

  @Test
  void should_reuse_assertion_helper_with_have_exactly() {
    // GIVEN
    Consumer<String> assertions = value -> assertThat(value).hasSize(2);
    var condition = new SatisfyingCondition<>(assertions);
    // WHEN/THEN
    assertThat(List.of("a", "bb", "c")).haveExactly(1, condition);
  }

  @Test
  void should_report_wrong_match_count_with_custom_description() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).hasSize(2));
    condition.as("has two characters");
    // WHEN
    var error = expectAssertionError(() -> assertThat(List.of("aa", "bb", "c")).haveExactly(1, condition));
    // THEN
    then(error).hasMessageContaining("has two characters");
  }

  @Test
  void should_work_with_composed_conditions() {
    // GIVEN
    var condition = new SatisfyingCondition<String>(value -> assertThat(value).hasSize(2));
    var combined = AllOf.allOf(condition, new Condition<String>(value -> value.startsWith("a"), "starts with a"));
    // WHEN
    boolean matches = combined.matches("ab");
    // THEN
    then(matches).isTrue();
    then(combined.matches("bb")).isFalse();
  }
}
