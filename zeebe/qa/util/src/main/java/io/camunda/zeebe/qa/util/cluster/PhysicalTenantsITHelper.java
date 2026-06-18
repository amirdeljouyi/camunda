/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.qa.util.cluster;

import io.camunda.client.CamundaClientBuilder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Reusable helper for booting a single {@link TestStandaloneBroker} that serves several physical
 * tenants, and for creating clients scoped to each of them. Every physical tenant, including the
 * {@code default} one, must be declared explicitly with its secondary-storage type. The helper only
 * emits configuration and builds clients; the broker lifecycle stays with the existing
 * {@code @ZeebeIntegration} / {@code @TestZeebe} extension:
 *
 * <pre>{@code
 * @ZeebeIntegration
 * final class MyIsolationIT {
 *   private static final PhysicalTenantsITHelper TENANTS =
 *       PhysicalTenantsITHelper.builder()
 *           .withTenant("default", "none")
 *           .withTenant("tenanta", "none")
 *           .build();
 *
 *   @TestZeebe
 *   private final TestStandaloneBroker broker =
 *       TENANTS.configure(new TestStandaloneBroker().withUnauthenticatedAccess());
 *
 *   @Test
 *   void shouldIsolate() {
 *     try (final var client = TENANTS.newClientBuilder(broker, "tenanta").build()) {
 *       // ...
 *     }
 *   }
 * }
 * }</pre>
 */
public final class PhysicalTenantsITHelper {

  public static final String DEFAULT_TENANT_ID = "default";
  public static final String STORAGE_NONE = "none";

  private final Map<String, String> tenants;

  private PhysicalTenantsITHelper(final Map<String, String> tenants) {
    this.tenants = Collections.unmodifiableMap(new LinkedHashMap<>(tenants));
  }

  public static Builder builder() {
    return new Builder();
  }

  public TestStandaloneBroker configure(final TestStandaloneBroker broker) {
    tenants.forEach(
        (tenant, storageType) -> {
          if (DEFAULT_TENANT_ID.equals(tenant)) {
            broker.withProperty("camunda.data.secondary-storage.type", storageType);
          } else {
            final String prefix = "camunda.physical-tenants." + tenant + ".";
            broker.withProperty(prefix + "data.secondary-storage.type", storageType);
            broker.withProperty(
                prefix + "security.initialization.default-roles.admin.users[0]", tenant + "-admin");
          }
        });
    return broker;
  }

  public CamundaClientBuilder newClientBuilder(
      final TestGateway<?> gateway, final String tenantId) {
    final CamundaClientBuilder builder = gateway.newClientBuilder();
    if (!DEFAULT_TENANT_ID.equals(tenantId)) {
      builder.physicalTenantId(tenantId);
    }
    return builder;
  }

  public Set<String> tenantIds() {
    return new LinkedHashSet<>(tenants.keySet());
  }

  public static final class Builder {

    private final Map<String, String> tenants = new LinkedHashMap<>();

    private Builder() {}

    public Builder withTenant(final String tenantId, final String storageType) {
      tenants.put(tenantId, storageType);
      return this;
    }

    public PhysicalTenantsITHelper build() {
      return new PhysicalTenantsITHelper(tenants);
    }
  }
}
