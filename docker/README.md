Entrypoint variables
=====================
ZOOKEEPER_HOST: Indicate the ip or fqdn of the zookeeper host (i.e. zk.demo.stratio.com)
EXECUTION_MODE: Indicates the execution mode of the module. It can take three values:
    - local: It will execute locally, either using a local spark context or a spark cluster. (Not needed to setup, it is the default value)
    - mesos: It will execute using a mesos cluster.
    - yarn: It will execute using a yarn cluster.
MESOS_MASTER: Only needed if EXECUTION_MODE is 'mesos'. Indicates the ip or fqdn of the Mesos master host (i.e. mesos.demo.stratio.com:7077).
HDFS_MASTER: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the ip or fqdn of the Yarn master host.
HDFS_PORT: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the port of the Yarn master host.
SPARK_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the spark version.
HADOOP_SPARK_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the compiled hadoop spark version.
HADOOP_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the hadoop version.
SPARK_MASTER: Only needed if EXECUTION_MODE is 'standalone'. Indicates the ip or fqdn of the Spark master host (i.e. spark.demo.stratio.com:7077).

Usage examples
===============
- Local execution:
docker run -dit --name sp --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com/stratio/sparta:0.11.0
  
- Mesos execution:
docker run -dit --name sp --env EXECUTION_MODE=mesos --env MESOS_MASTER=mm.demo.stratio.com --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com/stratio/sparta:0.11.0
  
- Yarn execution:
docker run -dit --name sp --env EXECUTION_MODE=yarn --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com/stratio/sparta:0.11.0
