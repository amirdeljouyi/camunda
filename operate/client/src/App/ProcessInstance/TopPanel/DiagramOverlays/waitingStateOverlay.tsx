/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

/* eslint-disable react-refresh/only-export-components -- overlay modules intentionally co-locate their data hook, config, and renderer in a single file */

import {useMemo} from 'react';
import {WAITING_BADGE} from 'modules/bpmn-js/badgePositions';
import {WaitingStateOverlay} from 'modules/components/WaitingStateOverlay';
import {useProcessInstance} from 'modules/queries/processInstance/useProcessInstance';
import {useElementInstanceInspection} from 'modules/queries/elementInstanceInspection/useElementInstanceInspection';
import {getWaitStateLabel} from 'modules/utils/waitStates';
import {getClientConfig} from 'modules/utils/getClientConfig';
import {useProcessInstancePageParams} from '../../useProcessInstancePageParams';
import {useFirstAgentInstancePerElement} from './agentInstances';
import type {DiagramOverlay, OverlayModule} from './types';

const TYPE = 'waitingState';

type WaitingStatePayload = {
  label: string;
};

const useOverlaysData: OverlayModule['useOverlaysData'] = ({
  isModificationModeEnabled,
}) => {
  const clientConfig = getClientConfig();
  const {processInstanceId = ''} = useProcessInstancePageParams();
  const {data: processInstance} = useProcessInstance();
  const {data: inspectionData} = useElementInstanceInspection({
    processInstanceKey: processInstanceId,
    enabled:
      clientConfig.waitStatesEnabled && processInstance?.state === 'ACTIVE',
  });
  const {elementsWithAgent} = useFirstAgentInstancePerElement();

  return useMemo(() => {
    if (isModificationModeEnabled || !inspectionData?.items?.length) {
      return [];
    }

    // Group wait states by elementId (show only 1 label per element)
    const waitStatesByElement = new Map<string, typeof inspectionData.items>();
    for (const item of inspectionData.items) {
      const existing = waitStatesByElement.get(item.elementId) ?? [];
      existing.push(item);
      waitStatesByElement.set(item.elementId, existing);
    }

    const overlays = [];
    for (const [elementId, waitStates] of waitStatesByElement) {
      // Hide the waiting state when an agent instance exists for the element.
      if (elementsWithAgent.has(elementId)) {
        continue;
      }
      const label = getWaitStateLabel(waitStates);
      if (label) {
        overlays.push({
          elementId,
          type: TYPE,
          position: WAITING_BADGE,
          payload: {label} satisfies WaitingStatePayload,
        });
      }
    }

    return overlays;
  }, [isModificationModeEnabled, inspectionData, elementsWithAgent]);
};

const Renderer: React.FC<{overlay: DiagramOverlay}> = ({overlay}) => {
  const payload = overlay.payload as WaitingStatePayload;

  return (
    <WaitingStateOverlay container={overlay.container} label={payload.label} />
  );
};

const waitingStateOverlay: OverlayModule = {
  type: TYPE,
  useOverlaysData,
  getKey: (overlay) => `waiting-${overlay.elementId}`,
  Renderer,
};

export {waitingStateOverlay};
