/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {shallow} from 'enzyme';
import {Link} from '@carbon/react';

import ExportFilterHint from './ExportFilterHint';

const DOCS_BASE =
  'https://docs.camunda.io/docs/self-managed/components/orchestration-cluster/zeebe/exporters/elasticsearch-exporter/';

it('should render an info toggletip with the localized hint text and icon label', () => {
  const node = shallow(<ExportFilterHint variant="variable" />);

  expect(node.find('Toggletip')).toExist();
  expect(node.find('ToggletipButton').prop('label')).toContain('export filtering');
  expect(node.find('span').text()).toContain('exporter configuration');
});

it('should link the variable variant to the variable-name filter documentation securely', () => {
  const node = shallow(<ExportFilterHint variant="variable" />);

  const link = node.find(Link);

  expect(link.prop('href')).toBe(DOCS_BASE + '#variable-name-filters');
  // prevents reverse-tabnabbing on the external docs link
  expect(link.prop('target')).toBe('_blank');
  expect(link.prop('rel')).toBe('noopener noreferrer');
});

it('should link the report-setup variant to the bpmn process filter documentation', () => {
  const node = shallow(<ExportFilterHint variant="reportSetup" />);

  expect(node.find(Link).prop('href')).toBe(DOCS_BASE + '#bpmn-process-filters');
  expect(node.find('span').text()).toContain('processes and variables');
});
