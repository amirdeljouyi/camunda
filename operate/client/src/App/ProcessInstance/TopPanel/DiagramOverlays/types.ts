/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import type {OverlayData} from 'modules/bpmn-js/overlayTypes';

/**
 * An overlay as it lives in the {@link diagramOverlaysStore} once the diagram has
 * mounted a container for it. This is what an overlay renderer receives.
 */
type DiagramOverlay = {
  payload?: unknown;
  container: HTMLElement;
  elementId: string;
  type: string;
};

/**
 * Context handed to every overlay module when it builds its data, so a module
 * can decide whether it should contribute overlays in the current diagram mode.
 */
type OverlayModuleContext = {
  isModificationModeEnabled: boolean;
};

/**
 * Self-contained definition of a single diagram overlay type. Co-locates how the
 * overlay data is derived (`useOverlaysData`) with how each overlay is rendered
 * (`Renderer`).
 *
 * To add a new overlay type: create a module that satisfies this contract and
 * register it in `overlayModules` (see ./registry). Nothing else needs to change.
 */
type OverlayModule = {
  /** Unique discriminator, also stored on every overlay of this type. */
  type: string;
  /**
   * Builds the overlay data for this type. Implemented as a hook so it can read
   * from queries/stores. Must follow the rules of hooks (called unconditionally).
   * Return an empty array when the overlay should not appear in the current mode.
   */
  useOverlaysData: (context: OverlayModuleContext) => OverlayData[];
  /** Stable React key for a single overlay of this type. */
  getKey: (overlay: DiagramOverlay) => string;
  /** Renders a single overlay of this type into its diagram container. */
  Renderer: React.FC<{overlay: DiagramOverlay}>;
};

export type {DiagramOverlay, OverlayModule, OverlayModuleContext};
