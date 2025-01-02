/*
 * Copyright (c) 2021-2025. caoccao.com Sam Cao
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
    const val GROUP_ID = "com.caoccao.javet"
    const val NAME = "Javenode"
    const val VERSION = Versions.JAVENODE
    const val URL = "https://github.com/caoccao/Javenode"

    object Pom {
        const val ARTIFACT_ID = "javenode"
        const val DESCRIPTION =
            "Javenode is Java + V8 + Node.js. It is a Node.js simulator with Java in V8."

        object Developer {
            const val ID = "caoccao"
            const val EMAIL = "sjtucaocao@gmail.com"
            const val NAME = "Sam Cao"
            const val ORGANIZATION = "caoccao.com"
            const val ORGANIZATION_URL = "https://www.caoccao.com"
        }

        object License {
            const val NAME = "APACHE LICENSE, VERSION 2.0"
            const val URL = "https://github.com/caoccao/Javenode/blob/main/LICENSE"
        }

        object Scm {
            const val CONNECTION = "scm:git:git://github.com/Javenode.git"
            const val DEVELOPER_CONNECTION = "scm:git:ssh://github.com/Javenode.git"
        }
    }

    object Projects {
        const val JAVET = "com.caoccao.javet:javet-core:${Versions.JAVET}"
        const val JAVET_NODE_LINUX_ARM64 = "com.caoccao.javet:javet-node-linux-arm64:${Versions.JAVET}"
        const val JAVET_NODE_LINUX_X86_64 = "com.caoccao.javet:javet-node-linux-x86_64:${Versions.JAVET}"
        const val JAVET_NODE_MACOS_ARM64 = "com.caoccao.javet:javet-node-macos-arm64:${Versions.JAVET}"
        const val JAVET_NODE_MACOS_X86_64 = "com.caoccao.javet:javet-node-macos-x86_64:${Versions.JAVET}"
        const val JAVET_NODE_WINDOWS_X86_64 = "com.caoccao.javet:javet-node-windows-x86_64:${Versions.JAVET}"
        const val JAVET_V8_LINUX_ARM64 = "com.caoccao.javet:javet-v8-linux-arm64:${Versions.JAVET}"
        const val JAVET_V8_LINUX_X86_64 = "com.caoccao.javet:javet-v8-linux-x86_64:${Versions.JAVET}"
        const val JAVET_V8_MACOS_ARM64 = "com.caoccao.javet:javet-v8-macos-arm64:${Versions.JAVET}"
        const val JAVET_V8_MACOS_X86_64 = "com.caoccao.javet:javet-v8-macos-x86_64:${Versions.JAVET}"
        const val JAVET_V8_WINDOWS_X86_64 = "com.caoccao.javet:javet-v8-windows-x86_64:${Versions.JAVET}"

        // https://mvnrepository.com/artifact/com.caoccao.javet.buddy/javet-buddy
        const val JAVET_BUDDY = "com.caoccao.javet.buddy:javet-buddy:${Versions.JAVET_BUDDY}"

        // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
        const val JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT}"

        // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT}"

        // https://mvnrepository.com/artifact/io.vertx/vertx-core
        const val VERTX = "io.vertx:vertx-core:${Versions.VERTX}"
    }

    object Versions {
        const val BYTE_BUDDY = "1.14.10"
        const val JAVA_VERSION = "1.8"
        const val JAVET = "4.0.0"
        const val JAVET_BUDDY = "0.2.0"
        const val JAVENODE = "0.9.0"
        const val JUNIT = "5.10.1"
        const val VERTX = "4.4.6"
    }
}

val buildDir = layout.buildDirectory.get().toString()

plugins {
    java
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = Config.GROUP_ID
version = Config.VERSION

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    val os = OperatingSystem.current()
    val cpuArch = System.getProperty("os.arch")
    val isArm64 = cpuArch == "aarch64" || cpuArch == "arm64";
    compileOnly(Config.Projects.JAVET)
    testImplementation(Config.Projects.JAVET)
    if (os.isLinux) {
        if (isArm64) {
            compileOnly(Config.Projects.JAVET_NODE_LINUX_ARM64)
            testImplementation(Config.Projects.JAVET_NODE_LINUX_ARM64)
            compileOnly(Config.Projects.JAVET_V8_LINUX_ARM64)
            testImplementation(Config.Projects.JAVET_V8_LINUX_ARM64)
        } else {
            compileOnly(Config.Projects.JAVET_NODE_LINUX_X86_64)
            testImplementation(Config.Projects.JAVET_NODE_LINUX_X86_64)
            compileOnly(Config.Projects.JAVET_V8_LINUX_X86_64)
            testImplementation(Config.Projects.JAVET_V8_LINUX_X86_64)
        }
    } else if (os.isMacOsX) {
        if (isArm64) {
            compileOnly(Config.Projects.JAVET_NODE_MACOS_ARM64)
            testImplementation(Config.Projects.JAVET_NODE_MACOS_ARM64)
            compileOnly(Config.Projects.JAVET_V8_MACOS_ARM64)
            testImplementation(Config.Projects.JAVET_V8_MACOS_ARM64)
        } else {
            compileOnly(Config.Projects.JAVET_NODE_MACOS_X86_64)
            testImplementation(Config.Projects.JAVET_NODE_MACOS_X86_64)
            compileOnly(Config.Projects.JAVET_V8_MACOS_X86_64)
            testImplementation(Config.Projects.JAVET_V8_MACOS_X86_64)
        }
    } else if (os.isWindows && !isArm64) {
        compileOnly(Config.Projects.JAVET_NODE_WINDOWS_X86_64)
        testImplementation(Config.Projects.JAVET_NODE_WINDOWS_X86_64)
        compileOnly(Config.Projects.JAVET_V8_WINDOWS_X86_64)
        testImplementation(Config.Projects.JAVET_V8_WINDOWS_X86_64)
    }
    implementation(Config.Projects.JAVET_BUDDY)
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

publishing {
    publications {
        create<MavenPublication>("generatePom") {
            from(components["java"])
            pom {
                artifactId = Config.Pom.ARTIFACT_ID
                description.set(Config.Pom.DESCRIPTION)
                groupId = Config.GROUP_ID
                name.set(Config.NAME)
                url.set(Config.URL)
                version = Config.VERSION
                licenses {
                    license {
                        name.set(Config.Pom.License.NAME)
                        url.set(Config.Pom.License.URL)
                    }
                }
                developers {
                    developer {
                        id.set(Config.Pom.Developer.ID)
                        email.set(Config.Pom.Developer.EMAIL)
                        name.set(Config.Pom.Developer.NAME)
                        organization.set(Config.Pom.Developer.ORGANIZATION)
                        organizationUrl.set(Config.Pom.Developer.ORGANIZATION_URL)
                    }
                }
                scm {
                    connection.set(Config.Pom.Scm.CONNECTION)
                    developerConnection.set(Config.Pom.Scm.DEVELOPER_CONNECTION)
                    tag.set(Config.Versions.JAVENODE)
                    url.set(Config.URL)
                }
                properties.set(
                    mapOf(
                        "maven.compiler.source" to Config.Versions.JAVA_VERSION,
                        "maven.compiler.target" to Config.Versions.JAVA_VERSION,
                    )
                )
            }
        }
    }
}

tasks {
    withType(Test::class.java) {
        useJUnitPlatform()
    }
    withType<GenerateMavenPom> {
        destination = file("$buildDir/libs/${Config.Pom.ARTIFACT_ID}-${Config.VERSION}.pom")
    }
}
