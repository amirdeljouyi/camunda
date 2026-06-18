/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Camunda License 1.0. You may not use this file
 * except in compliance with the Camunda License 1.0.
 */
package io.camunda.zeebe.qa.util.cluster;

import io.camunda.client.CamundaClientBuilder;
import io.camunda.configuration.SecondaryStorage.SecondaryStorageType;
import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Reusable helper for booting a single {@link TestStandaloneBroker} that serves several physical
 * tenants, and for addressing each of them over gRPC and REST. Every physical tenant, including the
 * {@code default} one, must be declared explicitly with its {@link Storage}. The helper only emits
 * configuration and builds clients / REST base URLs; the broker lifecycle stays with the existing
 * {@code @ZeebeIntegration} / {@code @TestZeebe} extension:
 *
 * <pre>{@code
 * @ZeebeIntegration
 * final class MyIsolationIT {
 *   private static final PhysicalTenantsITHelper TENANTS =
 *       PhysicalTenantsITHelper.builder()
 *           .withTenant("default", Storage.rdbmsH2("default"))
 *           .withTenant("tenanta", Storage.rdbmsH2("tenanta"))
 *           .build();
 *
 *   @TestZeebe
 *   private final TestStandaloneBroker broker =
 *       TENANTS.configure(new TestStandaloneBroker().withUnauthenticatedAccess());
 *
 *   @Test
 *   void shouldIsolate() {
 *     try (final var client = TENANTS.newClientBuilder(broker, "tenanta").build()) {
 *       // gRPC scoped to tenanta
 *     }
 *     final URI restBase = TENANTS.restBaseFor(broker, "tenanta"); // .../physical-tenants/tenanta/v2
 *   }
 * }
 * }</pre>
 */
public final class PhysicalTenantsITHelper {

  public static final String DEFAULT_TENANT_ID = "default";

  private final Map<String, Storage> tenants;

  private PhysicalTenantsITHelper(final Map<String, Storage> tenants) {
    this.tenants = Collections.unmodifiableMap(new LinkedHashMap<>(tenants));
  }

  public static Builder builder() {
    return new Builder();
  }

  public TestStandaloneBroker configure(final TestStandaloneBroker broker) {
    tenants.forEach(
        (tenant, storage) -> {
          storage.applyTo(broker, tenant);
          if (!DEFAULT_TENANT_ID.equals(tenant)) {
            broker.withProperty(
                "camunda.physical-tenants."
                    + tenant
                    + ".security.initialization.default-roles.admin.users[0]",
                tenant + "-admin");
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

  public URI restBaseFor(final TestGateway<?> gateway, final String tenantId) {
    final String base = gateway.restAddress().toString().replaceAll("/+$", "");
    final String tenantPrefix =
        DEFAULT_TENANT_ID.equals(tenantId) ? "" : "/physical-tenants/" + tenantId;
    return URI.create(base + tenantPrefix + "/v2");
  }

  public Set<String> tenantIds() {
    return new LinkedHashSet<>(tenants.keySet());
  }

  public static final class Builder {

    private final Map<String, Storage> tenants = new LinkedHashMap<>();

    private Builder() {}

    public Builder withTenant(final String tenantId, final Storage storage) {
      tenants.put(tenantId, storage);
      return this;
    }

    public PhysicalTenantsITHelper build() {
      return new PhysicalTenantsITHelper(tenants);
    }
  }

  private record NoneStorage() implements Storage {

    @Override
    public void applyTo(final TestStandaloneBroker broker, final String tenantId) {
      if (DEFAULT_TENANT_ID.equals(tenantId)) {
        broker.withSecondaryStorageType(SecondaryStorageType.none);
      } else {
        broker.withProperty(
            "camunda.physical-tenants." + tenantId + ".data.secondary-storage.type",
            SecondaryStorageType.none.name());
      }
    }
  }

  private record RdbmsStorage(String url, String username, String password) implements Storage {

    @Override
    public void applyTo(final TestStandaloneBroker broker, final String tenantId) {
      if (DEFAULT_TENANT_ID.equals(tenantId)) {
        broker.withSecondaryStorageType(SecondaryStorageType.rdbms);
        broker.withDataConfig(
            data -> {
              final var rdbms = data.getSecondaryStorage().getRdbms();
              rdbms.setUrl(url);
              rdbms.setUsername(username);
              rdbms.setPassword(password);
            });
      } else {
        final String prefix = "camunda.physical-tenants." + tenantId + ".data.secondary-storage.";
        broker.withProperty(prefix + "type", SecondaryStorageType.rdbms.name());
        broker.withProperty(prefix + "rdbms.url", url);
        broker.withProperty(prefix + "rdbms.username", username);
        broker.withProperty(prefix + "rdbms.password", password);
      }
    }
  }

  public interface Storage {

    void applyTo(TestStandaloneBroker broker, String tenantId);

    static Storage none() {
      return new NoneStorage();
    }

    static Storage rdbms(final String url, final String username, final String password) {
      return new RdbmsStorage(url, username, password);
    }

    static Storage rdbmsH2(final String dbName) {
      return rdbms(
          "jdbc:h2:mem:" + dbName + "-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
          "sa",
          "");
    }
  }
}
