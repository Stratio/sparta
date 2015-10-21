Stratio Sparkta Cheat Sheet
########

Running Sparkta
===============

First of all, you must have Zookeeper running before to start Stratio Sparkta.

* Start Sparkta

  * Manually::

    $ cd /opt/sds/sparkta
    $ ./bin/run

  * Service::

    $ sudo service sparkta start


* Open in browser Sparkta Web: http://localhost:9090
* Create an input
* Create an output
* Create a policy
* Run it!

Policy
======

A policy is the way we tell Sparkta how to aggregate data. It is in JSON format and you can check some
|examples_link| in the official repository.

.. |examples_link| raw:: html

   <a href="https://github.com/Stratio/sparkta/tree/master/examples/policies" target="_blank">examples</a>

It consists of the following parts:

* Input: :doc:`inputs`
* Transformation(s): :doc:`transformations`
* Cube(s): :doc:`cube`
* Output(s): :doc:`outputs`

Sandbox
=======

If you prefer, you can test Sparkta in a VM environment. We distribute a sandbox to play with it: :ref:`sandbox-label`
