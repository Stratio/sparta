Entrypoint variables
=====================
ZOOKEEPER_HOST: Indicate the ip or fqdn of the zookeeper host (i.e. zk.demo.stratio.com)
EXECUTION_MODE: Indicates the execution mode of the module. It can take three values:
    - local: It will execute locally, either using a local spark context or a spark cluster. (Not needed to setup, it is the default value)
    - mesos: It will execute using a mesos cluster.
    - yarn: It will execute using a yarn cluster.
SPARK_HOME: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the path of the spark compilation for hadoop.
MESOS_MASTER: Only needed if EXECUTION_MODE is 'mesos'. Indicates the ip or fqdn of the Mesos master host.
HDFS_MASTER: Only needed if EXECUTION_MODE is 'mesos' or 'yarn'. Indicates the ip or fqdn of the Yarn master host.

Usage examples
===============
- Local execution:
docker run -dit --name sp --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com:5000/stratio/sparta:0.9.0
  
- Mesos execution:
docker run -dit --name sp --env EXECUTION_MODE=mesos --env SPARK_HOME=/spark-1.5.2-bin-hadoop2.6 --env MESOS_MASTER=mm.demo.stratio.com --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com:5000/stratio/sparta:0.9.0
  
- Yarn execution:
docker run -dit --name sp --env EXECUTION_MODE=yarn --env SPARK_HOME=/spark-1.5.2-bin-hadoop2.6 --volumes-from hs --env HDFS_MASTER=hm.demo.stratio.com --env ZOOKEEPER_HOST=zk.demo.stratio.com qa.stratio.com:5000/stratio/sparta:0.9.0
