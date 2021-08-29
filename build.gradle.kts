/*
 * Copyright (c) 2021. caoccao.com Sam Cao
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

plugins {
    java
    `java-library`
}

repositories {
    mavenCentral()
}

group = "com.caoccao.javet"
version = "0.1.0"


repositories {
    mavenCentral()
}

dependencies {
    val JAVET_VERSION = "0.9.9"
    if (OperatingSystem.current().isMacOsX()) {
        implementation("com.caoccao.javet:javet-macos:${JAVET_VERSION}")
    } else {
        implementation("com.caoccao.javet:javet:${JAVET_VERSION}")
    }
    implementation("io.reactivex.rxjava3:rxjava:3.1.0")
    implementation("cglib:cglib:3.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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

tasks.withType<Javadoc>{
    options.encoding = "UTF-8"
}
