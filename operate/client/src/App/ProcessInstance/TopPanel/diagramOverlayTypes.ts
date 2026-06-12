/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

const OVERLAY_TYPE_STATE = 'elementState';
const OVERLAY_TYPE_MODIFICATIONS_BADGE = 'modificationsBadge';
const OVERLAY_TYPE_WAITING_STATE = 'waitingState';
const OVERLAY_TYPE_AGENT_STATUS = 'agentStatus';
const OVERLAY_TYPE_AGENT_SHINE = 'agentShine';

type ModificationBadgePayload = {
  newTokenCount: number;
  cancelledTokenCount: number;
};

export {
  OVERLAY_TYPE_STATE,
  OVERLAY_TYPE_MODIFICATIONS_BADGE,
  OVERLAY_TYPE_WAITING_STATE,
  OVERLAY_TYPE_AGENT_STATUS,
  OVERLAY_TYPE_AGENT_SHINE,
};
export type {ModificationBadgePayload};
