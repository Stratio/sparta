Configuration
*********************

Overview
========

This section shows how to configure Sparkta through Typesafe's standard Config.

By default the system will use the config file "reference.conf", but you can overwrite this values having and
application.conf in your classpath. This is the summary of all parameters that you could configure:

* :ref:`zk-config`

* :ref:`spray-config`

* :ref:`api-config`

* :ref:`swagger-config`

* :ref:`cluster-config`

   * :ref:`cluster-config-standalone`

   * :ref:`cluster-config-mesos`

   * :ref:`cluster-config-yarn`

More deep documentation about it, can be found at |typesafe_config_repo|.

.. |typesafe_config_repo| raw:: html

   <a href="https://github.com/typesafehub/config"
   target="_blank">Github Typesafe's repository</a>

A reference.conf with a full example of a configuration can be found at |github_sparkta_repo_reference_conf|.

.. |github_sparkta_repo_reference_conf| raw:: html

   <a href="https://github.com/Stratio/Sparkta/blob/master/serving-api/src/main/resources/reference.conf"
   target="_blank">Sparkta repository</a>

.. _zk-config:

Zookeeper configuration
=======================

One of the necessities of Sparkta is to keep track of all policies running and their status. Since Zookeeper is a
Sparkta dependency and a common piece in any Big Data architecture we save all this metadata in Zookeeper nodes.

All this info is saved under the znode `/stratio/sparkta`. Under this root we save input and output fragments,
policies and policy statuses.

ZK is used intensely. For example:

- To save fragments (inputs, outputs, etc.) that will be used by a policy.
- Policies.
- PolicyContexts (states of a run job).
- Etc.

An example in the application.conf::

  zk {
    connectionString = "localhost:2181"
    connectionTimeout = 15000
    sessionTimeout = 60000
    retryAttempts = 5
    retryInterval = 10000
  }

+--------------------+-------------------------------------------------------------------+
| Property           | Description                                                       |
+====================+===================================================================+
| connectionString   | A connection string containing a comma separated list of          |
|                    | host:port pairs, each corresponding to a ZooKeeper server.        |
+--------------------+-------------------------------------------------------------------+
| connectionTimeout  | The maximum session timeout in milliseconds that the server will  |
|                    | allow the client to negotiate.                                    |
+--------------------+-------------------------------------------------------------------+
| sessionTimeout     | If for some reason, the client fails to send heart beats to the   |
|                    | server for a prolonged period of time (exceeding the              |
|                    | sessionTimeout value, for instance), the server will expire the   |
|                    | session, and the session ID will become invalid.                  |
+--------------------+-------------------------------------------------------------------+
| retryAttempts      | Number of connection retries.                                     |
+--------------------+-------------------------------------------------------------------+
| retryInterval      | Interval between connection retries, in milliseconds.             |
+--------------------+-------------------------------------------------------------------+

.. _spray-config:

Spray.io configuration
======================

Spray is a toolkit for building REST/HTTP-based integration layers on top of Scala and Akka and is the main door to
offer a Sparkta API to the world.

More deep documentation about it, can be found at |spray_io|.

.. |spray_io| raw:: html

   <a href="https://github.com/spray/spray"
   target="_blank">Spray.io repository</a>

An example in the application.conf::

  spray.routing {
    verbose-error-messages = on
    render-vanity-footer = no
  }
  spray.can {
    verbose-error-messages = on
  }

.. _api-config:

API configuration
=================

Parameters related to the API REST.

An example in the application.conf::

  api {
    host = localhost
    port = 9090
  }

+----------+----------------------------------------+
| Property | Description                            |
+==========+========================================+
| host     | Hostname where the server will be bind.|
+----------+----------------------------------------+
| port     | Port where the server will be bind.    |
+----------+----------------------------------------+

.. _swagger-config:

Swagger configuration
=====================

Swagger is a Spray.io's submodule that it is used to generate automatically documentation about API's endpoints.

More deep documentation about it, can be found at |swagger|.

.. |swagger| raw:: html

   <a href="https://github.com/gettyimages/spray-swagger"
   target="_blank">Swagger repository</a>

An example in the application.conf::

  swagger {
    host = localhost
    port = 9091
  }

+----------+----------------------------------------+
| Property | Description                            |
+==========+========================================+
| host     | Hostname where the server will be bind.|
+----------+----------------------------------------+
| port     | Port where the server will be bind.    |
+----------+----------------------------------------+

.. _cluster-config:

Cluster configuration
=====================

If you have the need of to run Sparkta in a cluster, you have three possibilities:

- Sparkta + Spark Standalone: only one policy per cluster. (*)
- Sparkta + Apache Mesos: one or more policy/policies per cluster. (*)
- Sparkta + Apache YARN: one or more policy/policies per cluster. (*)

(*) Note: it is impossible to run up several spark contexts in the same machine in Standalone mode because you just
can run only one policy per cluster. However if you choose Mesos or YARN each job is treated in an isolate environment,
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

.. _cluster-config-standalone:

Sparkta + Spark Standalone
--------------------------

You must have correctly configured and deployed Spark Standalone. You can obtain information about how to do it
|spark_standalone_doc|.

.. |spark_standalone_doc| raw:: html

   <a href="http://spark.apache.org/docs/latest/spark-standalone.html"
   target="_blank">here</a>

.. _cluster-config-mesos:

Sparkta + Apache Mesos
----------------------

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

.. _cluster-config-yarn:

Sparkta + Apache YARN
---------------------

You must have correctly configured and deployed Apache Yarn. You can obtain information about how to do it at
|yarn_doc|.

.. |yarn_doc| raw:: html

   <a href="https://hadoop.apache.org/docs/current/hadoop-yarn/hadoop-yarn-site/YARN.html"
   target="_blank">here</a>

Fragments
=====

Fragments are JSON blocks of inputs/outputs that will be included in a policy. If one fragment is changed, all policies that had included it, will be automatically changed too. In fact, it is a nice way to reuse inputs/outputs between policies. An example of an input fragment::

  {
    "fragmentType": "input",
    "name": "twitter",
    "description": "twitter input",
    "shortDescription": "twitter input",
    "icon": "icon.png",
    "element": {
      "name": "in-twitter",
      "type": "Twitter",
      "configuration": {
        "consumerKey": "*",
        "consumerSecret": "*",
        "accessToken": "*",
        "accessTokenSecret": "*"
      }
    }
  }

These fragments are saved in Zookeeper in the following paths `/stratio/sparkta/fragments/input` and
`/stratio/sparkta/fragments/output`

Policies are saved in the following path `/stratio/sparkta/policies`.

Another useful information we save is the policy status. We save the current status of a policy. This status is
persisted in path `/stratio/sparkta/contexts`
