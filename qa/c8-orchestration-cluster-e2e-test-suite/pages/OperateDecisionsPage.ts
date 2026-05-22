/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */

import {Page, Locator} from '@playwright/test';

class OperateDecisionsPage {
  private page: Page;
  readonly decisionViewer: Locator;
  readonly decisionNameFilter: Locator;
  readonly decisionVersionFilter: Locator;
<<<<<<< HEAD
  readonly viewDecisionInstanceLink: (decisionInstanceId: string) => Locator;
=======
  readonly decisionViewer: Locator;
  readonly decisionInstanceKeysFilter: Locator;
  readonly filterRegion: Locator;
  readonly clearSelectedItemButton: Locator;
  readonly moreFiltersButton: Locator;
  readonly evaluatedCheckbox: Locator;
  readonly failedCheckbox: Locator;
  readonly decisionInstancesList: Locator;
>>>>>>> ad83a1af (test: add E2E coverage for filter functionality in DMN Decision view)

  constructor(page: Page) {
    this.page = page;
    this.decisionViewer = page.getByTestId('decision-viewer');
    this.decisionNameFilter = page.getByRole('combobox', {name: 'Name'});
    this.decisionVersionFilter = page.getByRole('combobox', {
      name: 'Version',
    });
    this.viewDecisionInstanceLink = (decisionInstanceId: string) =>
      page.getByRole('link', {
        name: `View decision instance ${decisionInstanceId}`,
      });
  }

<<<<<<< HEAD
  async selectDecisionName(name: string): Promise<void> {
    await this.decisionNameFilter.click();
    await this.page.getByRole('option', {name, exact: true}).click();
  }

  async selectVersion(version: string): Promise<void> {
    await this.decisionVersionFilter.click();
    await this.page.getByRole('option', {name: version, exact: true}).click();
  }

  async clearComboBox(): Promise<void> {
    await this.page.getByRole('button', {name: 'Clear selected item'}).click();
=======
    this.decisionNameFilter = page.getByRole('combobox', {
      name: 'Name',
    });
    this.decisionVersionFilter = page.getByRole('combobox', {
      name: 'Version',
    });
    this.decisionInstanceKeysFilter = page.getByLabel(
      /^decision instance key\(s\)$/i,
    );
    this.decisionViewer = page.getByTestId('decision-viewer');
    this.filterRegion = page.getByRole('region', {name: /filter/i});
    this.clearSelectedItemButton = page.getByRole('button', {
      name: 'Clear selected item',
    });
    this.moreFiltersButton = page.getByRole('button', {name: 'More Filters'});
    this.evaluatedCheckbox = page.locator('label').filter({hasText: 'Evaluated'});
    this.failedCheckbox = page.locator('label').filter({hasText: 'Failed'});
    this.decisionInstancesList = page.getByTestId('data-list');
>>>>>>> ad83a1af (test: add E2E coverage for filter functionality in DMN Decision view)
  }

  async clickViewDecisionInstanceLink(
    decisionInstanceId: string,
  ): Promise<void> {
    await this.viewDecisionInstanceLink(decisionInstanceId).click();
  }
<<<<<<< HEAD
=======

  async gotoDecisionsPage(options?: {
    searchParams?: SearchParams;
  }): Promise<void> {
    if (!options?.searchParams) {
      await this.page.goto('/decisions');
      return;
    }

    const searchParams = new URLSearchParams();
    Object.entries(options.searchParams).forEach(([key, value]) => {
      if (value !== undefined) {
        searchParams.append(key, value);
      }
    });

    await this.page.goto(`/decisions?${searchParams.toString()}`);
  }

  async selectDecisionName(option: string): Promise<void> {
    await this.decisionNameFilter.click();
    await this.filterRegion
      .getByRole('option', {name: option, exact: true})
      .click();
  }

  async selectVersion(option: string): Promise<void> {
    await this.decisionVersionFilter.click();
    await this.filterRegion
      .getByRole('option', {name: option, exact: true})
      .click();
  }

  async clearComboBox(): Promise<void> {
    await this.clearSelectedItemButton.click();
  }

  async clickEvaluatedCheckbox(): Promise<void> {
    await this.evaluatedCheckbox.click();
  }

  async clickFailedCheckbox(): Promise<void> {
    await this.failedCheckbox.click();
  }

  async displayOptionalFilter(filterName: OptionalFilter): Promise<void> {
    await this.moreFiltersButton.click();
    await this.page
      .getByRole('menuitem', {
        name: filterName,
      })
      .click();
  }
>>>>>>> ad83a1af (test: add E2E coverage for filter functionality in DMN Decision view)
}

export {OperateDecisionsPage};
