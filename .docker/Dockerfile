# syntax=docker/dockerfile-upstream:master-labs <- required for --parents in COPY

# Builder
FROM eclipse-temurin:23-jdk AS builder
WORKDIR /var/local/workspace

# Copy all module definitions to download dependencies. Improves layer caching in src only changes
COPY --parents gradle.properties gradlew gradle/ **/build.gradle.kts **/settings.gradle.kts .

# Extract dependencies into output folder
RUN ./gradlew copyDependencies --no-daemon

# Copy the rest, mostly the source files
COPY . .

# Build the project
RUN ./gradlew -Psirenhunts.libpath="libs" build --no-daemon

# The runtime image
FROM docker.knockturnmc.com/knockturn/runtime:1.0.0-java

# Copy the libs
COPY --from=builder /var/local/workspace/build/libs/dependencies /var/lib/app/libs
COPY --from=builder /var/local/workspace/build/libs/*.jar /var/lib/app/app.jar

ENTRYPOINT ["java", "-jar", "/var/lib/app/app.jar"]