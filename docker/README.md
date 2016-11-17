Entrypoint variables
=====================
ZOOKEEPER_HOST: Indicate the ip or fqdn of the zookeeper host (i.e. zk.demo.stratio.com:2181)
EXECUTION_MODE: Indicates the execution mode of the module. It can take three values:
    - local: It will execute locally, either using a local spark context or a spark cluster. (Not needed to setup, it is the default value)
    - mesos: It will execute using a mesos cluster.
    - yarn: It will execute using a yarn cluster.
STOP_GRACEFULLY: Stop condition when finish Spark Contexts
REMEMBER_PARTITIONER: Remember partitioner in stateful operations in Spark
AUTO_DELETE_CHECKPOINT: auto delete checkpoint path when run policies
MESOS_MASTER: Only needed if EXECUTION_MODE is 'mesos'. Indicates the ip or fqdn of the Mesos master host (i.e. mesos.demo.stratio.com:7077).
HDFS_MASTER: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the ip or fqdn of the Yarn master host.
HDFS_PORT: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the port of the Yarn master host. (i.e. 8020)
HDFS_USER_NAME: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the hadoop user name. (i.e. stratio)
HDFS_PRINCIPAL_NAME: Integration with Kerberos: this variable is used to identify as an user Sparta with Kerberos
HDFS_PRINCIPAL_NAME_SUFFIX: Integration with Kerberos: this variable is used to identify as an user Sparta with Kerberos
HDFS_PRINCIPAL_NAME_PREFIX: Integration with Kerberos: this variable is used to identify as an user Sparta with Kerberos
HDFS_KEYTAB: Integration with Kerberos: this variable is used to specify the path of the keytab.
HDFS_KEYTAB_RELOAD: Integration with Kerberos: this variable is used to specify periodicy to reload the path of the keytab.
SPARK_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the spark version. (i.e. spark-1.6.2)
HADOOP_SPARK_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the compiled hadoop spark version. (i.e. hadoop2.6)
HADOOP_VERSION: Only needed if EXECUTION_MODE is 'mesos', 'yarn' or 'standalone'. Indicates the hadoop version. (i.e. hadoop-2.7.1)
SPARK_MASTER: Only needed if EXECUTION_MODE is 'standalone' or 'local'. Indicates the ip or fqdn of the Spark master host (i.e. spark.demo.stratio.com:7077 or local[2]).

Usage examples
===============
- Local execution:
docker run -dit --name sp --env ZOOKEEPER_HOST=zk.demo.stratio.com stratio/sparta:latest
  
- Mesos execution:
docker run -dit --name sp --env EXECUTION_MODE=mesos --env MESOS_MASTER=mm.demo.stratio.com --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com stratio/sparta:latest
  
- Yarn execution:
docker run -dit --name sp --env EXECUTION_MODE=yarn --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com stratio/sparta:latest
