/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.variable;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.protocol.impl.encoding.MsgPackConverter;
import io.camunda.zeebe.protocol.record.RejectionType;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

final class VariableNestingDepthValidatorTest {

  // ---------------------------------------------------------------------------
  // exceedsMaxDepth — algorithm correctness
  // ---------------------------------------------------------------------------

  @Test
  void shouldNotExceedDepthForFlatDocument() {
    // given
    final var buffer = msgPackOf("{\"a\": 1, \"b\": 2}");

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 1)).isFalse();
  }

  @Test
  void shouldNotExceedDepthAtExactLimit() {
    // given — document with exactly 1000 nesting levels
    final var buffer = msgPackOf(buildNestedJson(1000));

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 1000)).isFalse();
  }

  @Test
  void shouldExceedDepthWhenOneLevelOverLimit() {
    // given — document with 1001 nesting levels
    final var buffer = msgPackOf(buildNestedJson(1001));

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 1000)).isTrue();
  }

  @Test
  void shouldShortCircuitOnVeryDeepDocument() {
    // given — 50 000 levels deep, well above the default limit of 1 000.
    // This must return a rejection without throwing StackOverflowError, proving
    // the depth walk is iterative.
    final var buffer = msgPackOf(buildNestedJson(50_000));

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 1000)).isTrue();
  }

  @Test
  void shouldHandleArrayNesting() {
    // given — {"a": [[[1]]]} is 4 levels deep (root map + 3 arrays)
    final var buffer = msgPackOf("{\"a\": [[[1]]]}");

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 4)).isFalse();
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 3)).isTrue();
  }

  @Test
  void shouldHandleMixedNesting() {
    // given — {"a": [{"b": 1}]} is 3 levels deep (root map + array + inner map)
    final var buffer = msgPackOf("{\"a\": [{\"b\": 1}]}");

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 3)).isFalse();
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 2)).isTrue();
  }

  @Test
  void shouldHandleEmptyNestedContainers() {
    // given — {"a": {}, "b": []} — both inner containers are depth 2
    final var buffer = msgPackOf("{\"a\": {}, \"b\": []}");

    // when / then
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 2)).isFalse();
    assertThat(VariableNestingDepthValidator.exceedsMaxDepth(buffer, 1)).isTrue();
  }

  // ---------------------------------------------------------------------------
  // validate — rejection path
  // ---------------------------------------------------------------------------

  @Test
  void shouldReturnRightForNullBuffer() {
    // when
    final var result = VariableNestingDepthValidator.validate(null, 1000);

    // then
    assertThat(result.isRight()).isTrue();
  }

  @Test
  void shouldReturnRightForEmptyBuffer() {
    // given
    final var buffer = new UnsafeBuffer(new byte[0]);

    // when
    final var result = VariableNestingDepthValidator.validate(buffer, 1000);

    // then
    assertThat(result.isRight()).isTrue();
  }

  @Test
  void shouldReturnRightWhenDocumentIsWithinLimit() {
    // given
    final var buffer = msgPackOf(buildNestedJson(1000));

    // when
    final var result = VariableNestingDepthValidator.validate(buffer, 1000);

    // then
    assertThat(result.isRight()).isTrue();
  }

  @Test
  void shouldReturnLeftWhenDocumentExceedsLimit() {
    // given
    final var buffer = msgPackOf(buildNestedJson(1001));

    // when
    final var result = VariableNestingDepthValidator.validate(buffer, 1000);

    // then
    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().type()).isEqualTo(RejectionType.INVALID_ARGUMENT);
    assertThat(result.getLeft().reason())
        .isEqualTo(
            VariableNestingDepthValidator.NESTING_DEPTH_EXCEEDED_ERROR_MESSAGE.formatted(1000));
  }

  @Test
  void shouldNotThrowStackOverflowOnPathologicalInput() {
    // given — 50 000 levels deep: validates that the iterative algorithm handles extreme input
    final var buffer = msgPackOf(buildNestedJson(50_000));

    // when / then — must not throw
    final var result = VariableNestingDepthValidator.validate(buffer, 1000);

    assertThat(result.isLeft()).isTrue();
    assertThat(result.getLeft().type()).isEqualTo(RejectionType.INVALID_ARGUMENT);
  }

  // ---------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------

  /**
   * Builds a JSON document with {@code depth} levels of map nesting, e.g. {@code depth=3} produces
   * {@code {"k":{"k":{"k":1}}}}.
   */
  private static String buildNestedJson(final int depth) {
    final var sb = new StringBuilder();
    for (int i = 0; i < depth; i++) {
      sb.append("{\"k\":");
    }
    sb.append("1");
    for (int i = 0; i < depth; i++) {
      sb.append("}");
    }
    return sb.toString();
  }

  private static UnsafeBuffer msgPackOf(final String json) {
    final byte[] bytes = MsgPackConverter.convertToMsgPack(json);
    return new UnsafeBuffer(bytes);
  }
}
