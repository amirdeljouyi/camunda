/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {Information} from '@carbon/icons-react';
import {Link, Toggletip, ToggletipActions, ToggletipButton, ToggletipContent} from '@carbon/react';

import {useDocs} from 'hooks';
import {t} from 'translation';

// Process and variable export can be filtered at the exporter/cluster level (independent of
// Optimize), which affects which processes, variables and results appear. These links point users
// at the relevant exporter docs. They live under the self-managed docs root (not the Optimize docs
// root), so we build them from the base docs URL rather than via DocsLink (which prefixes /optimize).
const EXPORTER_DOCS_BASE =
  'docs/self-managed/components/orchestration-cluster/zeebe/exporters/elasticsearch-exporter/';

// The contexts the hint is shown in. Each maps to its own explanation and docs anchor:
// - variable: variable filters (variable-name export filtering)
// - reportSetup: report setup, where whole processes can also be excluded
const VARIANTS = {
  variable: {textKey: 'common.exportFilterHint.variableText', docsAnchor: '#variable-name-filters'},
  reportSetup: {
    textKey: 'common.exportFilterHint.reportSetupText',
    docsAnchor: '#bpmn-process-filters',
  },
} as const;

interface ExportFilterHintProps {
  variant: keyof typeof VARIANTS;
}

export default function ExportFilterHint({variant}: ExportFilterHintProps): JSX.Element {
  const {getBaseDocsUrl} = useDocs();
  const {textKey, docsAnchor} = VARIANTS[variant];

  return (
    <Toggletip className="ExportFilterHint" align="bottom">
      <ToggletipButton label={t('common.exportFilterHint.iconLabel').toString()}>
        <Information />
      </ToggletipButton>
      <ToggletipContent>
        <span>{t(textKey)}</span>
        <ToggletipActions>
          <Link
            href={getBaseDocsUrl() + EXPORTER_DOCS_BASE + docsAnchor}
            target="_blank"
            rel="noopener noreferrer"
          >
            {t('common.seeDocs')}
          </Link>
        </ToggletipActions>
      </ToggletipContent>
    </Toggletip>
  );
}
