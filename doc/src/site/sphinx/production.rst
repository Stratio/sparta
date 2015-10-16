Production Checklist
####################

In this section we provide you the production environment basic settings.

First of all, you should check the different systems and services  availability in your environment.

Then to configure our service you need to take a look in two files.

application.conf
-------
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

