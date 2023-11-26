=============
Release Notes
=============

0.3.0
-----

* Upgraded Javet to v3.0.2
* Removed all reflection calls to improve performance

0.2.0
-----

* Upgraded Javet to v3.0.0

0.1.1
-----

* Added ``JNEventLoopOptions``
* Added module ``console``
    * ``assert(value[, ...message])``
    * ``clear()``
    * ``count([label])``
    * ``countReset([label])``
    * ``debug([data][, ...args])``
    * ``error([data][, ...args])``
    * ``info([data][, ...args])``
    * ``log([data][, ...args])``
    * ``time([label])``
    * ``timeEnd([label])``
    * ``timeLog([label][, ...data])``
    * ``trace([message][, ...args])``
    * ``warn([data][, ...args])``

0.1.0
-----

* Added ``JNEventLoop``
* Added module ``timers``
    * ``clearImmediate(immediate)``
    * ``clearInterval(timeout)``
    * ``clearTimeout(timeout)``
    * ``setImmediate(callback[, ...args])``
    * ``setInterval(callback[, delay[, ...args]])``
    * ``setTimeout(callback[, delay[, ...args]])``
* Added module ``timers/promises``
    * ``setImmediate([value])``
    * ``setInterval([delay[, value]])``
    * ``setTimeout([delay[, value]])``
