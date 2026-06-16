import buildlogic.parsePomProperties
import buildlogic.pomVersion

// Scoped version overrides for Optimize modules — mirrors Maven parent POM dependency management
// scoped to optimize/* only, preventing leakage into non-optimize modules.
val optimizePom = parsePomProperties(rootDir.resolve("optimize/pom.xml").readText())

fun optVersion(key: String) = pomVersion(optimizePom, key)

configurations.all {
  resolutionStrategy.force(
    // Versions hardcoded in optimize/backend/pom.xml <dependency> blocks (no pom property exists)
    "com.github.sisyphsu:dateparser:1.0.11",
    "com.icegreen:greenmail:2.1.8",
    "com.opencsv:opencsv:5.12.0",
    "com.sun.mail:jakarta.mail:2.0.2",
    "com.tdunning:t-digest:3.3",
    "com.vdurmont:semver4j:3.1.0",
    "io.github.netmikey.logunit:logunit-log4j2:2.0.0",
    "io.github.netmikey.logunit:logunit-core:2.0.0",
    "org.apache.lucene:lucene-core:8.11.3",
    "org.eclipse.angus:jakarta.mail:2.0.5",
    "org.elasticsearch:elasticsearch:7.17.29",
    "org.glassfish.jersey.core:jersey-client:4.0.2",
    "org.glassfish.jersey.media:jersey-media-json-jackson:4.0.2",
    // Versions from optimize/pom.xml <properties>
    "org.mock-server:mockserver-client-java:${optVersion("mockserver.version")}",
    "org.mock-server:mockserver-core:${optVersion("mockserver.version")}",
    "org.mock-server:mockserver-netty:${optVersion("mockserver.version")}",
    "org.mock-server:mockserver-junit-jupiter:${optVersion("mockserver.version")}",
    "org.mockito:mockito-inline:${optVersion("mockito-inline.version")}",
    "org.quartz-scheduler:quartz:${optVersion("quartz.version")}",
  )
}
