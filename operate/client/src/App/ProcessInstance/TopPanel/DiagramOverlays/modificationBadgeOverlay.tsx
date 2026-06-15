/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

/* eslint-disable react-refresh/only-export-components -- overlay modules intentionally co-locate their data hook, config, and renderer in a single file */

import {useMemo} from 'react';
import {MODIFICATIONS} from 'modules/bpmn-js/badgePositions';
import {useModificationsByElement} from 'modules/hooks/modifications';
import {ModificationBadgeOverlay} from '../ModificationBadgeOverlay';
import type {DiagramOverlay, OverlayModule} from './types';

const TYPE = 'modificationsBadge';

type ModificationBadgePayload = {
  newTokenCount: number;
  cancelledTokenCount: number;
};

const useOverlaysData: OverlayModule['useOverlaysData'] = ({
  isModificationModeEnabled,
}) => {
  const modificationsByElement = useModificationsByElement();

  return useMemo(() => {
    if (!isModificationModeEnabled) {
      return [];
    }

    return Object.entries(modificationsByElement).map(
      ([elementId, tokens]) => ({
        elementId,
        type: TYPE,
        position: MODIFICATIONS,
        payload: {
          newTokenCount: tokens.newTokens,
          cancelledTokenCount: tokens.visibleCancelledTokens,
        } satisfies ModificationBadgePayload,
      }),
    );
  }, [isModificationModeEnabled, modificationsByElement]);
};

const Renderer: React.FC<{overlay: DiagramOverlay}> = ({overlay}) => {
  const payload = overlay.payload as ModificationBadgePayload;

  return (
    <ModificationBadgeOverlay
      container={overlay.container}
      newTokenCount={payload.newTokenCount}
      cancelledTokenCount={payload.cancelledTokenCount}
    />
  );
};

const modificationBadgeOverlay: OverlayModule = {
  type: TYPE,
  useOverlaysData,
  getKey: (overlay) => overlay.elementId,
  Renderer,
};

export {modificationBadgeOverlay};
export type {ModificationBadgePayload};
