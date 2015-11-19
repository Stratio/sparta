Sparkta in production
******************

Production Checklist
====================

In this section we provide you the production environment basic settings.

First of all, you should check the different systems and services availability in your environment.

Then to configure our service you need to take a look at two files.

application.conf
----------------
Where? -> /etc/sds/sparkta/application.conf

What should you check?

#. Network interfaces and ports
#. Zookeeper configuration
#. Hdfs paths
#. Resource manager configuration (mesos/yarn/standalone/local)
#. Spark configurations

run.sh
------
Where? -> /opt/sds/sparkta/bin/run.sh

What should you check?

#. Memory configuration
#. Jmx port
#. Other Java opts

Finally you should check your inputs and outputs availability and their configuration in your policies.


Troubleshooting
===============

This section tries to group the common errors our users find. Most of them are easily fixed checking the logs.
Sparta logs are at `/var/log/sds/sparkta/sparkta.[out|err]` . Please if you find an error that belongs to Sparkta don't
hesitate in open an issue at |github_dl|

.. |github_dl| raw:: html

   <a href="https://github.com/Stratio/Sparkta/issues" target="_blank">GitHub</a>



**Error when a cube has a dimension of type DateTime**::

    java.lang.ClassCastException: java.lang.Long cannot be cast to java.util.Date
            at com.stratio.sparkta.plugin.field.datetime.DateTimeField.precisionValue(DateTimeField.scala:58)
            at com.stratio.sparkta.aggregator.CubeOperations$$anonfun$extractDimensionsAggregations$1$$anonfun$1$$anonfun$apply$1.apply(CubeMaker.scala:75)
            at com.stratio.sparkta.aggregator.CubeOperations$$anonfun$extractDimensionsAggregations$1$$anonfun$1$$anonfun$apply$1.apply(CubeMaker.scala:74)
            at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:244)
            at scala.collection.TraversableLike$$anonfun$map$1.apply(TraversableLike.scala:244)
            at scala.collection.immutable.List.foreach(List.scala:318)

When one of your dimensions is of the type DateTime the field this dimension is pointing to must be a date. The
solution is to `parse <transformations.html#_datetime-transformation-label>`__ this field to be in Date format.



**When checkpoint path points to a local folder**::

    ERROR JobScheduler - Error running job streaming job 1436519460000 ms.0
    org.apache.spark.SparkException: Job aborted due to stage failure:
    Task 1 in stage 5.0 failed 1 times, most recent failure:
    Lost task 1.0 in stage 5.0 (TID 4, localhost): java.io.IOException:
    Failed to create local dir in
    /tmp/spark-0c463f24-e058-4fb6-b211-438228b962fa/blockmgr-11ab4a0d-f35e-4f3a-8852-f0814ff44476/30.

The problem is the following , Sparkta hasn't got enough privileges to write the checkpoint files.
There are two feasible solutions:

- execute as sudo or root "$SPARKTA_HOME/sudo bin/run"
- set the checkpoint dir in the policy json pointing where you have permission to write



**When I execute vagrant up, an error occurs**::

    A host only network interface you're attempting to configure via DHCP
    already has a conflicting host only adapter with DHCP enabled. The
    DHCP on this adapter is incompatible with the DHCP settings. Two
    host only network interfaces are not allowed to overlap, and each
    host only network interface can have only one DHCP server. Please
    reconfigure your host only network or remove the virtual machine
    using the other host only network.

Deleting all conflicting network interfaces in VirtualBox should fix this issue.

Open VirtualBox > General Setup > Network > Tab "Host-only interfaces".




**NOTE:** If your issue is more detailed and has not been covered here, please check the rest of the website to see if it has
been answered, especially the `F.A.Q <faq.html>`__ page.