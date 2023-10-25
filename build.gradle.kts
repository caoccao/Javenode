/*
 * Copyright (c) 2021-2023. caoccao.com Sam Cao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.gradle.internal.os.OperatingSystem

object Config {
    object Projects {
        const val JAVET = "com.caoccao.javet:javet:${Versions.JAVET}"
        const val JAVET_LINUX_ARM64 = "com.caoccao.javet:javet-linux-arm64:${Versions.JAVET}"
        const val JAVET_MACOS = "com.caoccao.javet:javet-macos:${Versions.JAVET}"
        const val JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_JUPITER}"
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}"
        const val VERTX = "io.vertx:vertx-core:${Versions.VERTX}"
    }

    object Versions {
        const val JAVET = "3.0.0"
        const val JUNIT_JUPITER = "5.10.0"
        const val VERTX = "4.1.3"
    }
}

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
}

group = "com.caoccao.javet"
version = "0.2.0"


repositories {
    mavenCentral()
}

dependencies {
    val os = OperatingSystem.current()
    val cpuArch = System.getProperty("os.arch")
    if (os.isMacOsX) {
        implementation(Config.Projects.JAVET_MACOS)
    } else if (os.isLinux && (cpuArch == "aarch64" || cpuArch == "arm64")) {
        implementation(Config.Projects.JAVET_LINUX_ARM64)
    } else {
        implementation(Config.Projects.JAVET)
    }
    implementation(Config.Projects.VERTX)
    testImplementation(Config.Projects.JUNIT_JUPITER_API)
    testRuntimeOnly(Config.Projects.JUNIT_JUPITER_ENGINE)
}

afterEvaluate {
    tasks.withType(JavaCompile::class) {
        options.compilerArgs.add("-Xlint:unchecked")
        options.compilerArgs.add("-Xlint:deprecation")
    }
}

tasks.test {
    useJUnitPlatform {
        excludeTags("performance")
    }
}

tasks.register<Test>("performanceTest") {
    useJUnitPlatform {
        includeTags("performance")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    withJavadocJar()
}
