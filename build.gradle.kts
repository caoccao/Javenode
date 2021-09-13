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

object Config {
    object Projects {
        const val CGLIB = "cglib:cglib:${Versions.CGLIB}"
        const val JAVET = "com.caoccao.javet:javet:${Versions.JAVET}"
        const val JAVET_MACOS = "com.caoccao.javet:javet-macos:${Versions.JAVET}"
        const val JUNIT_JUPITER_API = "org.junit.jupiter:junit-jupiter-api:${Versions.JUNIT_JUPITER}"
        const val JUNIT_JUPITER_ENGINE = "org.junit.jupiter:junit-jupiter-engine:${Versions.JUNIT_JUPITER}"
        const val RXJAVA = "io.reactivex.rxjava3:rxjava:${Versions.RXJAVA}"
    }

    object Versions {
        const val CGLIB = "3.3.0"
        const val JAVET = "0.9.12"
        const val JUNIT_JUPITER = "5.7.0"
        const val RXJAVA = "3.1.0"
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
version = "0.1.0"


repositories {
    mavenCentral()
}

dependencies {
    implementation(Config.Projects.CGLIB)
    if (OperatingSystem.current().isMacOsX()) {
        implementation(Config.Projects.JAVET_MACOS)
    } else {
        implementation(Config.Projects.JAVET)
    }
    implementation(Config.Projects.RXJAVA)
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
