Configuration
=============

If you have the need of to run Sparkta in a cluster, you have three possibilities:

- Sparkta + Spark Standalone: only one policy per cluster. (*)
- Sparkta + Apache Mesos: one or more policy/policies per cluster. (*)
- Sparkta + Apache Yarn: one or more policy/policies per cluster. (*)

(*) Note: it is impossible to run up several spark contexts in the same machine in Standalone mode because you just
can run only one policy per cluster. However if you choose Mesos or Yarn each job is treated in an isolate environment,
therefore you could run up as many contexts as you wish.

In any of the selected choices it is mandatory to have HDFS installed and configured in all of nodes of the cluster.
Once this requirement is done, you must configure Sparkta in your application.conf::

  hdfs {
    hadoopUserName = stratio
    hadoopConfDir = /home/stratio/hadoop
    hdfsMaster = localhost
    pluginsFolder = "plugins"
    executionJarFolder = "driver"
    classpathFolder = "classpath"
  }

+--------------------+-------------------------------------------------------------------+
| Property           | Description                                                       |
+====================+===================================================================+
| hadoopUserName     | Name of the user that should be configured in Hadoop. The base    |
|                    | hdfs path will be /hadoopUserName.                                |
+--------------------+-------------------------------------------------------------------+
| hadoopConfDir      | Indicates the location of the Hadoop's configuration.             |
+--------------------+-------------------------------------------------------------------+
| hdfsMaster         | Host or IP of the hdfs master node.                               |
+--------------------+-------------------------------------------------------------------+
| pluginsFolder      | Hdfs path that contains all the plugins that the policy needs.    |
+--------------------+-------------------------------------------------------------------+
| executionJarFolder | Hdfs path that contains a jar with the driver that will run in    |
|                    | the cluster.                                                      |
+--------------------+-------------------------------------------------------------------+
| classpathFolder    | Hdfs path that contains other needed jars by the driver.          |
+--------------------+-------------------------------------------------------------------+

Sparkta + Spark Standalone
==========================

You must have correctly configured and deployed Spark StandAlone. You can obtain information about how to do it
|spark_standalone_doc|.

.. |spark_standalone_doc| raw:: html

   <a href="http://spark.apache.org/docs/latest/spark-standalone.html"
   target="_blank">here</a>

Sparkta + Apache Mesos
======================

You must have correctly configured and deployed Apache Mesos. You can obtain information about how to do it
|mesos_doc|.

.. |mesos_doc| raw:: html

   <a href="http://mesos.apache.org"
   target="_blank">here</a>

The next step is set up Sparkta's Mesos configuration::

  mesos {
    sparkHome = "/home/ubuntu/Descargas/spark-1.4.1-bin-hadoop2.6/"
    deployMode = cluster
    numExecutors = 2
    masterDispatchers = 127.0.0.1
    spark.streaming.concurrentJobs = 20
    spark.cores.max = 4
    spark.mesos.extra.cores = 1
    spark.mesos.coarse = true
    spark.executor.memory = 4G
    spark.driver.cores = 1
    spark.driver.memory= 4G
  }

+--------------------------------+--------------------------------------------------------------------+
| Property                       | Description                                                        |
+================================+====================================================================+
| sparkHome                      | Path that contains an installed Spark's distribution.              |
+--------------------------------+--------------------------------------------------------------------+
| deployMode                     | client | cluster (**).                                             |
+--------------------------------+--------------------------------------------------------------------+
| numExecutors                   | number of worker threads.                                          |
+--------------------------------+--------------------------------------------------------------------+
| masterDispatchers              | url of the MasterDispatchers (**).                                 |
+--------------------------------+--------------------------------------------------------------------+
| spark.streaming.concurrentJobs | number of simultaneous jobs that Spark Streaming could run.        |
+--------------------------------+--------------------------------------------------------------------+
| spark.cores.max                | the maximum amount of CPU cores to request for the application     |
|                                | from across the cluster (not from each machine).                   |
+--------------------------------+--------------------------------------------------------------------+
| spark.mesos.extra.cores        | set the extra amount of cpus to request per task. This setting is  |
|                                | only used for Mesos coarse grain mode. The total amount of cores   |
|                                | requested per task is the number of cores in the offer plus the    |
|                                | extra cores configured. Note that total amount of cores the        |
|                                | executor will request in total will not exceed the spark.cores.max |
+--------------------------------+--------------------------------------------------------------------+
| spark.mesos.coarse             | Set the run mode for Spark on Mesos.                               |
+--------------------------------+--------------------------------------------------------------------+
| spark.executor.memory          | Amount of memory to use per executor process (e.g. 2g, 8g).        |
+--------------------------------+--------------------------------------------------------------------+
| spark.driver.cores             | Number of cores to use for the driver  only in cluster mode.       |
+--------------------------------+--------------------------------------------------------------------+
| spark.driver.memory            | Amount of memory to use for the driver process.                    |
+--------------------------------+--------------------------------------------------------------------+

(**) Note: Spark on Mesos also supports cluster mode, where the driver is launched in the cluster and the client can
find the results of the driver from the Mesos Web UI. To use cluster mode, you must start the MesosClusterDispatcher
in your cluster via the sbin/start-mesos-dispatcher.sh script, passing in the Mesos master url (e.g: mesos://host:5050).

From the client, you can submit a job to Mesos cluster by running spark-submit and specifying the master url to the url
of the MesosClusterDispatcher (e.g: mesos://dispatcher:7077). You can view driver statuses on the Spark cluster Web UI.

Sparkta + Apache Yarn
=====================

You must have correctly configured and deployed Apache Yarn. You can obtain information about how to do it in
|yarn_doc|.

.. |yarn_doc| raw:: html

   <a href="https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html"
   target="_blank">here</a>

The next step is set up Sparkta's Yarn configuration:

  yarn {
  }

+--------------------------------+-------------------------------------------------------------------+
| Property                       | Description                                                       |
+================================+===================================================================+
