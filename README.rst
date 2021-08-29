Javenode
========

|Discord| |Donate|

.. |Discord| image:: https://img.shields.io/badge/join%20our-Discord-%237289DA%20
    :target: https://discord.gg/R4vvKU96gw

.. |Donate| image:: https://img.shields.io/badge/Donate-PayPal-green.svg
    :target: https://paypal.me/caoccao?locale.x=en_US

Javenode is Java + V8 + Node.js. It is a Node.js simulator with Java in V8.

Javenode is an extension of `Javet <https://github.com/caoccao/Javet>`_ (Java + V8). It aims at simulating Node.js with Java in Javet V8 mode. Why? Because Javet V8 mode is much more secure than the Node.js mode, but lacks of some basic features, e.g. `setTimeout`, `setInterval`, etc. So, these **must-have** API can be found in Javenode.

========================= ================= ================
Feature                   Javet             Javenode
========================= ================= ================
External Dependencies     No                Yes
Platform Dependent        Yes               No
========================= ================= ================

If you like my work, please **Star** this project. And, you may follow me `@sjtucaocao <https://twitter.com/sjtucaocao>`_, or visit http://caoccao.blogspot.com/. And the official support channel is at `discord <https://discord.gg/R4vvKU96gw>`_.

TODO
====

* To implement Java object proxy with cglib
* To implement `fs`
* To implement `timers`

License
=======

`APACHE LICENSE, VERSION 2.0 <LICENSE>`_.
