Javenode
========

|Maven Central| |Discord| |Donate|

|Linux Build|

.. |Maven Central| image:: https://img.shields.io/maven-central/v/com.caoccao.javet/javenode?style=for-the-badge
    :target: https://central.sonatype.com/artifact/com.caoccao.javet/javenode

.. |Discord| image:: https://img.shields.io/discord/870518906115211305?label=join%20our%20Discord&style=for-the-badge
    :target: https://discord.gg/R4vvKU96gw

.. |Donate| image:: https://img.shields.io/badge/Donate-Paypal-green?style=for-the-badge
    :target: https://paypal.me/caoccao?locale.x=en_US

.. |Linux Build| image:: https://github.com/caoccao/Javenode/actions/workflows/linux_build.yml/badge.svg
    :target: https://github.com/caoccao/Javenode/actions/workflows/linux_build.yml

Javenode is Java + V8 + Node.js. It is a Node.js simulator with Java in V8.

Javenode is an extension of `Javet <https://github.com/caoccao/Javet>`_ (Java + V8). It aims at simulating Node.js with Java in Javet V8 mode. Why? Because Javet V8 mode is much more secure than the Node.js mode, but lacks of some basic features, e.g. `setTimeout`, `setInterval`, etc. So, these **must-have** API can be found in Javenode.

========================= ================= ================
Feature                   Javet             Javenode
========================= ================= ================
External Dependencies     No                Yes
Platform Dependent        Yes               No
========================= ================= ================

If you like my work, please **Star** this project. And, you may follow me `@sjtucaocao <https://twitter.com/sjtucaocao>`_, or visit https://sjtucaocao.medium.com/ or http://caoccao.blogspot.com/. And the official support channel is at `discord <https://discord.gg/R4vvKU96gw>`_.

Major Features
==============

* Native Event Loop (vert.x)
* Same Modules as Node.js
* Modules
    * console
    * timers
    * timers/promises

Quick Start
===========

Dependency
----------

Maven
^^^^^

.. code-block:: xml

    <dependency>
        <groupId>com.caoccao.javet</groupId>
        <artifactId>javenode</artifactId>
        <version>0.3.0</version>
    </dependency>

Gradle Kotlin DSL
^^^^^^^^^^^^^^^^^

.. code-block:: kotlin

    implementation("com.caoccao.javet:javenode:0.3.0")

Gradle Groovy DSL
^^^^^^^^^^^^^^^^^

.. code-block:: groovy

    implementation 'com.caoccao.javet:javenode:0.3.0'

Hello Javenode (Static Import)
------------------------------

.. code-block:: java

    try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
         JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {
        eventLoop.loadStaticModules(JNModuleType.Console, JNModuleType.Timers);
        v8Runtime.getExecutor("const a = [];\n" +
                "setTimeout(() => a.push('Hello Javenode'), 10);").executeVoid();
        eventLoop.await();
        v8Runtime.getExecutor("console.log(a[0]);").executeVoid();
    }

Hello Javenode (Dynamic Import)
-------------------------------

.. code-block:: java

    try (V8Runtime v8Runtime = V8Host.getV8Instance().createV8Runtime();
         JNEventLoop eventLoop = new JNEventLoop(v8Runtime)) {
        eventLoop.loadStaticModules(JNModuleType.Console);
        eventLoop.registerDynamicModules(JNModuleType.TimersPromises);
        v8Runtime.getExecutor(
                "import { setTimeout } from 'timers/promises';\n" +
                        "const a = [];\n" +
                        "setTimeout(10, 'Hello Javenode')\n" +
                        "  .then(result => a.push(result));\n" +
                        "globalThis.a = a;").setModule(true).executeVoid();
        eventLoop.await();
        v8Runtime.getExecutor("console.log(a[0]);").executeVoid();
    }

TODO
====

* To implement `fetch`
* To implement `fs`

License
=======

`APACHE LICENSE, VERSION 2.0 <LICENSE>`_

Documents
=========

* `Javet <https://github.com/caoccao/Javet>`_
* `Javenode Document Portal <https://www.caoccao.com/Javenode/>`_
