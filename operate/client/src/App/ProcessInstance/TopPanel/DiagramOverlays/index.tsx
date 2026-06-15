/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {observer} from 'mobx-react';
import {diagramOverlaysStore} from 'modules/stores/diagramOverlays';
import {overlayModules} from './registry';

/**
 * Renders every overlay currently in the {@link diagramOverlaysStore} by
 * delegating to the matching overlay module's renderer.
 */
const DiagramOverlays: React.FC = observer(() => {
  const {overlays} = diagramOverlaysStore.state;

  return (
    <>
      {overlayModules.map(({type, getKey, Renderer}) =>
        overlays
          .filter((overlay) => overlay.type === type)
          .map((overlay) => (
            <Renderer key={getKey(overlay)} overlay={overlay} />
          )),
      )}
    </>
  );
});

export {DiagramOverlays};
