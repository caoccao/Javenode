========================
Times and Times Promises
========================

Timers
======

The ``timer`` module exposes a global API for scheduling functions to be called at some future period of time.

API
---

* ``clearImmediate(immediate)``
* ``clearInterval(timeout)``
* ``clearTimeout(timeout)``
* ``setImmediate(callback[, ...args])``
* ``setInterval(callback[, delay[, ...args]])``
* ``setTimeout(callback[, delay[, ...args]])``

Timers Promises
===============

The ``timers/promises`` API provides an alternative set of timer functions that return Promise objects.

API
---

* ``setImmediate([value])``
* ``setInterval([delay[, value]])``
* ``setTimeout([delay[, value]])``

Please refer to the `official doc <https://nodejs.org/dist/latest-v16.x/docs/api/timers.html>`_ for detail.
