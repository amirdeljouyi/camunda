/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

/* eslint-disable react-refresh/only-export-components -- overlay modules intentionally co-locate their data hook, config, and renderer in a single file */

import {useMemo} from 'react';
import {AGENT_STATUS_TAG} from 'modules/bpmn-js/badgePositions';
import {AgentStatusOverlay} from '../AgentStatusOverlay';
import {useFirstAgentInstancePerElement} from './agentInstances';
import type {AgentStatusPayload} from 'modules/bpmn-js/overlayTypes';
import type {DiagramOverlay, OverlayModule} from './types';

const TYPE = 'agentStatus';

const useOverlaysData: OverlayModule['useOverlaysData'] = ({
  isModificationModeEnabled,
}) => {
  const {agentInstances} = useFirstAgentInstancePerElement();

  return useMemo(() => {
    if (isModificationModeEnabled) {
      return [];
    }

    return agentInstances.map((agentInstance) => ({
      type: TYPE,
      elementId: agentInstance.elementId,
      position: AGENT_STATUS_TAG,
      payload: {
        status: agentInstance.status,
        agentInstanceKey: agentInstance.agentInstanceKey,
      } satisfies AgentStatusPayload,
    }));
  }, [isModificationModeEnabled, agentInstances]);
};

const Renderer: React.FC<{overlay: DiagramOverlay}> = ({overlay}) => {
  const payload = overlay.payload as AgentStatusPayload;

  return (
    <AgentStatusOverlay container={overlay.container} status={payload.status} />
  );
};

const agentStatusOverlay: OverlayModule = {
  type: TYPE,
  useOverlaysData,
  getKey: (overlay) =>
    `${(overlay.payload as AgentStatusPayload).agentInstanceKey}-status`,
  Renderer,
};

export {agentStatusOverlay};
