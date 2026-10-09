plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

kotlin {
    jvmToolchain(25)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.mongodb:mongodb-spring-session:4.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.3"))
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.testcontainers:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Monolith: the frontend bundle ships inside the backend jar as classpath:/static.
val frontendDir = rootProject.layout.projectDirectory.dir("frontend")
val frontendOut = layout.buildDirectory.dir("frontend")

val buildFrontend by tasks.registering(Exec::class) {
    workingDir = frontendDir.asFile
    inputs.dir(frontendDir.dir("src"))
    inputs.files(frontendDir.file("package.json"), frontendDir.file("index.html"), frontendDir.file("vite.config.ts"))
    outputs.dir(frontendOut)
    commandLine("sh", "-c", "npm install --no-audit --no-fund && npm run build")
}

sourceSets.main {
    resources.srcDir(frontendOut)
}

tasks.processResources {
    dependsOn(buildFrontend)
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    // dev profile seeds the pilot data (Louisa, Yasmina, Louis, Sophie)
    args("--spring.profiles.active=dev")
}
