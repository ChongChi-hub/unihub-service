plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}")
    }
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api(libs.springdoc.openapi.starter.webmvc.ui)
    api("org.springframework.data:spring-data-commons")

    compileOnly("io.github.openfeign:feign-core")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.github.openfeign:feign-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
