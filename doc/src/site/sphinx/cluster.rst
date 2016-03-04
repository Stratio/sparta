Cluster Execution
***********

In Sparta is possible to run policies in a Spark Cluster. Sparta needs one Cluster Manager to run, and this could be
Mesos, Yarn or Spark StandAlone.
With this mode the user can run a lot of policies in the same Spark cluster with more than one Spark context.

All the configuration parameters are inside the application.conf file.

  One example is::

        config {
          executionMode = mesos
          rememberPartitioner = true
          stopGracefully = true
        }
         local {
            spark.app.name = SPARTA
            spark.master = "local[4]"
            spark.cores.max = 4
            spark.executor.memory = 1024m
            spark.app.name = SPARTA
            spark.sql.parquet.binaryAsString = true
            spark.streaming.concurrentJobs = 5
         }
         hdfs {
            hadoopUserName = stratio
            hadoopConfDir = /home/ubuntu
            hdfsMaster = localhost
            pluginsFolder = "plugins"
            executionJarFolder = "jarDriver"
            classpathFolder = "classpath"
         }
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


If you change the executionMode parameter you can change the type of execution, their possible values are:
  - mesos
  - yarn
  - standAlone
  - local

For the execution in cluster mode, the machines must have configured the environments variables:
  - HADOOP_HOME
  - SPARK_HOME
  - SPARTA_HOME

  Notes :
    If you don't have SPARK_HOME variable you can specify the "sparkHome" in the mesos/yarn/standAlone section.
    If you don't have HADOOP_HOME variable you can specify the "hadoopConfDir" in the hdfs section for search the
    configurations files for HDFS.

In local/mesos/yarn execution all parameters that start with "spark." are passed to the SparkContext. In the example
this parameters are optionals.


  config::

    executionMode: mesos/yarn/StandAlone/local. Optional: No.

    rememberPartitioner: Remember partitioner for the stateful operations. Optional: Yes.

    stopGracefully: Stop the sparkContext gracefully. Optional: Yes.


  hdfs::

    hadoopUserName: User name configured in Hdfs for save temporal files and classpath for the policies. Optional: No.

    hadoopConfDir: Path for search the configurations files for HDFS. Optional: Yes.

    hdfsMaster: Ip for search the files in HDFS. Optional: No.

    pluginsFolder: Folder for save the jars of the plugins related to each policy. Optional: Yes.

    executionJarFolder = Folder for save the jar driver related to each policy. Optional: Yes.

    classpathFolder = Folder for save the jars classpath related to each policy. Optional: Yes.


  mesos::

    sparkHome: Path of the spark installation to search the sparkSubmit program. Optional: Yes.

    deployMode: It`s possible to run in a mesos with two different modes, client and cluster. Optional: No.

    numExecutors: The number of Sparta executors to deploy with mesos. Optional: No.

    masterDispatchers: IP of the machine that runs the mesos dispatcher script. Optional: No.

    spark.cores.max: The maximum amount of CPU cores to request for the application from across the cluster (not from
      each machine). If not set, the default will be infinite (all available cores) on Mesos. Optional: Yes.

    spark.mesos.extra.cores: Set the extra amount of cpus to request per task. This setting is only used for Mesos
      coarse grain mode. The total amount of cores requested per task is the number of cores in the offer plus the
      extra cores configured. Note that total amount of cores the executor will request in total will not exceed
      thespark.cores.max setting. Optional: Yes.

    spark.mesos.coarse: If set to "true", runs over Mesos clusters in "coarse-grained" sharing mode,
      where Spark acquires one long-lived Mesos task on each machine instead of one Mesos task per Spark task. This
      gives lower-latency scheduling for short queries, but leaves resources in use for the
       whole duration of the Spark job. Optional: Yes.

    spark.executor.memory: Amount of memory to use per executor process (e.g. 2g, 8g). Optional: Yes.

    spark.driver.cores: Number of cores to use for the driver process. Optional: Yes.

    spark.driver.memory: Amount of memory to use for the driver process, i.e. where SparkContext is initialized.
      (e.g. 1g, 2g)'
