/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {useMemo} from 'react';
import type {OverlayData} from 'modules/bpmn-js/overlayTypes';
import {overlayModules} from './registry';
import type {OverlayModuleContext} from './types';

/**
 * Builds the combined overlay data for the diagram by composing every overlay
 * module in the registry. The result is passed to
 * `<Diagram overlaysData={...} />`.
 */
const useDiagramOverlaysData = (
  isModificationModeEnabled: boolean,
): OverlayData[] => {
  const context: OverlayModuleContext = {isModificationModeEnabled};

  // The registry is static, so calling each module's hook here keeps a stable
  // hook order across renders (rules of hooks are respected).
  const overlaysDataPerType = overlayModules.map((module) =>
    module.useOverlaysData(context),
  );

  return useMemo(
    () => overlaysDataPerType.flat(),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    overlaysDataPerType,
  );
};

export {useDiagramOverlaysData};
