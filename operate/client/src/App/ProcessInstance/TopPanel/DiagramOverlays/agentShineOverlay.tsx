/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

/* eslint-disable react-refresh/only-export-components -- overlay modules intentionally co-locate their data hook, config, and renderer in a single file */

import {useMemo} from 'react';
import {AGENT_SHINE} from 'modules/bpmn-js/badgePositions';
import {AgentShineOverlay} from '../AgentShineOverlay';
import {useFirstAgentInstancePerElement} from './agentInstances';
import type {AgentShinePayload} from 'modules/bpmn-js/overlayTypes';
import type {DiagramOverlay, OverlayModule} from './types';

const TYPE = 'agentShine';

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
      position: AGENT_SHINE,
      payload: {
        agentInstanceKey: agentInstance.agentInstanceKey,
      } satisfies AgentShinePayload,
    }));
  }, [isModificationModeEnabled, agentInstances]);
};

const Renderer: React.FC<{overlay: DiagramOverlay}> = ({overlay}) => {
  return (
    <AgentShineOverlay
      container={overlay.container}
      elementId={overlay.elementId}
    />
  );
};

const agentShineOverlay: OverlayModule = {
  type: TYPE,
  useOverlaysData,
  getKey: (overlay) =>
    `${(overlay.payload as AgentShinePayload).agentInstanceKey}-shine`,
  Renderer,
};

export {agentShineOverlay};
