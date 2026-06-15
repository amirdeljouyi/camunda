/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {elementStateOverlay} from './elementStateOverlay';
import {modificationBadgeOverlay} from './modificationBadgeOverlay';
import {waitingStateOverlay} from './waitingStateOverlay';
import {agentStatusOverlay} from './agentStatusOverlay';
import {agentShineOverlay} from './agentShineOverlay';
import type {OverlayModule} from './types';

/**
 * Registry of every diagram overlay type. Each entry is a self-contained module
 * that knows how to build its data and how to render itself.
 *
 * To add a new overlay type, create a module satisfying {@link OverlayModule}
 * and add it here — both data building and rendering are wired automatically.
 */
const overlayModules: OverlayModule[] = [
  elementStateOverlay,
  modificationBadgeOverlay,
  waitingStateOverlay,
  agentStatusOverlay,
  agentShineOverlay,
];

export {overlayModules};
