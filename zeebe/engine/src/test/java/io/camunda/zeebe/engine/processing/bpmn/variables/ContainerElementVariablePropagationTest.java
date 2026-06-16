/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.engine.processing.bpmn.variables;

import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.zeebe.engine.util.EngineRule;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.protocol.record.intent.ProcessInstanceIntent;
import io.camunda.zeebe.protocol.record.intent.SignalSubscriptionIntent;
import io.camunda.zeebe.test.util.record.RecordingExporter;
import io.camunda.zeebe.test.util.record.RecordingExporterTestWatcher;
import java.util.List;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

public final class ContainerElementVariablePropagationTest {

  @ClassRule public static final EngineRule ENGINE = EngineRule.singlePartition();

  private static final String PROCESS_ID = "process";
  private static final String CHILD_PROCESS_ID = "child-process";
  private static final String CONTAINER_VAR = "containerVar";
  private static final String SIGNAL_NAME = "propagation-test-signal";
  private static final String SCRIPT_TASK_ID = "script-task";
  private static final String AD_HOC_SUB_PROCESS_ID = "ahsp";
  private static final String LOOP_COUNTER_VAR = "loopCounter";
  private static final String AD_HOC_ELEMENTS_VAR = "adHocSubProcessElements";

  @Rule public final TestWatcher watcher = new RecordingExporterTestWatcher();

  @Test
  public void shouldNotPropagateVariableFromEmbeddedSubProcess() {
    // given
    deploy(buildEmbeddedSubProcess(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariableNotPropagated(processInstanceKey, processInstanceKey);
  }

  @Test
  public void shouldPropagateVariableFromEmbeddedSubProcess() {
    // given
    deploy(buildEmbeddedSubProcess(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariablePropagated(processInstanceKey, CONTAINER_VAR);
  }

  @Test
  public void shouldNotPropagateVariableFromAdHocSubProcess() {
    // given
    deploy(buildAdHocSubProcess(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    activateAhspTask(processInstanceKey);

    // then
    assertVariableNotPropagated(processInstanceKey, processInstanceKey, AD_HOC_ELEMENTS_VAR);
  }

  @Test
  public void shouldPropagateVariableFromAdHocSubProcess() {
    // given
    deploy(buildAdHocSubProcess(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    activateAhspTask(processInstanceKey);

    // then
    assertVariablePropagated(processInstanceKey, CONTAINER_VAR);
  }

  @Test
  public void shouldNotPropagateVariableFromAdHocSubProcessInnerInstance() {
    // given
    deploy(buildAdHocSubProcessInnerInstance(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    activateAhspTask(processInstanceKey);

    // then
    assertVariableNotPropagated(
        processInstanceKey, adHocSubProcessKey(processInstanceKey), AD_HOC_ELEMENTS_VAR);
  }

  @Test
  public void shouldPropagateVariableFromAdHocSubProcessInnerInstance() {
    // given
    deploy(buildAdHocSubProcessInnerInstance(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    activateAhspTask(processInstanceKey);

    // then
    assertVariablePropagated(processInstanceKey, "results");
  }

  @Test
  public void shouldNotPropagateVariableFromEventSubProcess() {
    // given
    deploy(buildEventSubProcess(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    triggerEventSubProcess();

    // then
    assertVariableNotPropagated(processInstanceKey, processInstanceKey);
  }

  @Test
  public void shouldPropagateVariableFromEventSubProcess() {
    // given
    deploy(buildEventSubProcess(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // when
    triggerEventSubProcess();

    // then
    assertVariablePropagated(processInstanceKey, CONTAINER_VAR);
  }

  @Test
  public void shouldNotPropagateVariableFromCallActivity() {
    // given
    deploy(buildCallActivity(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariableNotPropagated(processInstanceKey, processInstanceKey);
  }

  @Test
  public void shouldPropagateVariableFromCallActivity() {
    // given
    deploy(buildCallActivity(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariablePropagated(processInstanceKey, CONTAINER_VAR);
  }

  @Test
  public void shouldNotPropagateVariableFromMultiInstance() {
    // given
    deploy(buildMultiInstance(false));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariableNotPropagated(processInstanceKey, processInstanceKey, LOOP_COUNTER_VAR);
  }

  @Test
  public void shouldPropagateVariableFromMultiInstance() {
    // given
    deploy(buildMultiInstance(true));
    final long processInstanceKey = ENGINE.processInstance().ofBpmnProcessId(PROCESS_ID).create();

    // then
    assertVariablePropagated(processInstanceKey, "results");
  }

  private static List<BpmnModelInstance> buildEmbeddedSubProcess(final boolean withOutputMapping) {
    return List.of(
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .subProcess(
                "subprocess",
                s -> {
                  if (withOutputMapping) {
                    s.zeebeOutputExpression(CONTAINER_VAR, CONTAINER_VAR);
                  }
                  s.embeddedSubProcess().startEvent().endEvent();
                })
            .endEvent()
            .done());
  }

  private static List<BpmnModelInstance> buildAdHocSubProcess(final boolean withOutputMapping) {
    return List.of(
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .adHocSubProcess(
                AD_HOC_SUB_PROCESS_ID,
                ahsp -> {
                  if (withOutputMapping) {
                    ahsp.zeebeOutputExpression(CONTAINER_VAR, CONTAINER_VAR);
                  }
                  ahsp.completionCondition("true");
                  ahsp.task(SCRIPT_TASK_ID);
                })
            .endEvent()
            .done());
  }

  private static List<BpmnModelInstance> buildAdHocSubProcessInnerInstance(
      final boolean withOutputMapping) {
    return List.of(
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .adHocSubProcess(
                AD_HOC_SUB_PROCESS_ID,
                ahsp -> {
                  ahsp.completionCondition("true");
                  if (withOutputMapping) {
                    ahsp.zeebeOutputCollection("results");
                    ahsp.zeebeOutputElementExpression(CONTAINER_VAR);
                  }
                  ahsp.scriptTask(
                      SCRIPT_TASK_ID,
                      t ->
                          t.zeebeExpression("=\"test-value\"")
                              .zeebeResultVariable("result")
                              .zeebeOutputExpression("result", CONTAINER_VAR));
                })
            .endEvent()
            .done());
  }

  private static List<BpmnModelInstance> buildEventSubProcess(final boolean withOutputMapping) {
    final var processBuilder = Bpmn.createExecutableProcess(PROCESS_ID);
    processBuilder.eventSubProcess(
        "event-subprocess",
        esp -> {
          if (withOutputMapping) {
            esp.zeebeOutputExpression(CONTAINER_VAR, CONTAINER_VAR);
          }
          esp.startEvent("signal-start").signal(SIGNAL_NAME).interrupting(true).endEvent();
        });
    return List.of(
        processBuilder
            .startEvent()
            .serviceTask("service", t -> t.zeebeJobType("block"))
            .endEvent()
            .done());
  }

  private static List<BpmnModelInstance> buildCallActivity(final boolean withOutputMapping) {
    final BpmnModelInstance childProcess =
        Bpmn.createExecutableProcess(CHILD_PROCESS_ID)
            .startEvent()
            .scriptTask(
                SCRIPT_TASK_ID,
                t ->
                    t.zeebeExpression("=\"test-value\"")
                        .zeebeResultVariable("result")
                        .zeebeOutputExpression("result", CONTAINER_VAR))
            .endEvent()
            .done();

    final BpmnModelInstance parent =
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .callActivity(
                "call",
                c -> {
                  c.zeebeProcessId(CHILD_PROCESS_ID);
                  if (withOutputMapping) {
                    c.zeebeOutputExpression(CONTAINER_VAR, CONTAINER_VAR);
                  } else {
                    // By default, all child variables propagate to parent. Disable it for
                    // isolation.
                    c.zeebePropagateAllChildVariables(false);
                  }
                })
            .endEvent()
            .done();

    return List.of(childProcess, parent);
  }

  private static List<BpmnModelInstance> buildMultiInstance(final boolean withOutputMapping) {
    return List.of(
        Bpmn.createExecutableProcess(PROCESS_ID)
            .startEvent()
            .scriptTask(
                SCRIPT_TASK_ID,
                t ->
                    t.zeebeExpression("=\"test-value\"")
                        .zeebeResultVariable("result")
                        .zeebeOutputExpression("result", CONTAINER_VAR)
                        .multiInstance(
                            m -> {
                              m.parallel().zeebeInputCollectionExpression("[\"item\"]");
                              if (withOutputMapping) {
                                m.zeebeInputElement("item")
                                    .zeebeOutputCollection("results")
                                    .zeebeOutputElementExpression(CONTAINER_VAR);
                              } else {
                                m.zeebeInputElement(CONTAINER_VAR);
                              }
                            }))
            .endEvent()
            .done());
  }

  private void triggerEventSubProcess() {
    RecordingExporter.signalSubscriptionRecords(SignalSubscriptionIntent.CREATED)
        .withSignalName(SIGNAL_NAME)
        .await();
    ENGINE.signal().withSignalName(SIGNAL_NAME).broadcast();
  }

  private void activateAhspTask(final long processInstanceKey) {
    ENGINE
        .adHocSubProcessActivity()
        .withAdHocSubProcessInstanceKey(adHocSubProcessKey(processInstanceKey))
        .withElementIds(SCRIPT_TASK_ID)
        .activate();
  }

  private void assertVariableNotPropagated(
      final long processInstanceKey,
      final long isolationScopeKey,
      final String... internalVariableNames) {
    assertThat(
            RecordingExporter.records()
                .limitToProcessInstance(processInstanceKey)
                .variableRecords()
                .withScopeKey(isolationScopeKey)
                .withName(CONTAINER_VAR)
                .exists())
        .isFalse();
    for (final String varName : internalVariableNames) {
      assertThat(
              RecordingExporter.records()
                  .limitToProcessInstance(processInstanceKey)
                  .variableRecords()
                  .withScopeKey(processInstanceKey)
                  .withName(varName)
                  .exists())
          .isFalse();
    }
  }

  private void assertVariablePropagated(
      final long processInstanceKey, final String propagatedVariableName) {
    assertThat(
            RecordingExporter.records()
                .limitToProcessInstance(processInstanceKey)
                .variableRecords()
                .withScopeKey(processInstanceKey)
                .withName(propagatedVariableName)
                .exists())
        .isTrue();
  }

  private static void deploy(final List<BpmnModelInstance> processes) {
    processes.forEach(p -> ENGINE.deployment().withXmlResource(p).deploy());
  }

  private static long adHocSubProcessKey(final long processInstanceKey) {
    return RecordingExporter.processInstanceRecords(ProcessInstanceIntent.ELEMENT_ACTIVATED)
        .withProcessInstanceKey(processInstanceKey)
        .withElementId(AD_HOC_SUB_PROCESS_ID)
        .getFirst()
        .getKey();
  }
}
