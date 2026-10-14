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

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenNullPointerException;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class SatisfyingCondition_constructor_Test {

  @Test
  void should_reject_null_consumer() {
    // GIVEN
    Consumer<String> assertions = null;
    // WHEN/THEN
    thenNullPointerException().isThrownBy(() -> new SatisfyingCondition<>(assertions))
                              .withMessage("The assertions consumer must not be null");
  }

  @Test
  void should_provide_default_description() {
    // GIVEN
    Consumer<String> assertions = value -> {};
    // WHEN
    var condition = new SatisfyingCondition<>(assertions);
    // THEN
    then(condition.description().value()).isEqualTo("satisfies the assertions");
  }
}
