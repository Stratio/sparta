Stratio Sparkta sandbox
*****************************

Vagrant Setup
=============

To get an operating virtual machine with Stratio Sparkta distribution up
and running, we use `Vagrant <https://www.vagrantup.com/>`__.

-  Download and install the lastest version (1.7.2)
   `Vagrant <https://www.vagrantup.com/downloads.html>`__.
-  Download and install
   `VirtualBox <https://www.virtualbox.org/wiki/Downloads>`__.
-  If you are in a windows machine, we will install
   `Cygwin <https://cygwin.com/install.html>`__.

Running the sandbox
===================

-  Initialize the current directory from the command line:
   **vagrant init stratio/sparkta**.
-  Start the sandbox from command line: **vagrant up**

Please, be patient the first time it runs!!

Login into the sandbox as **vagrant user** and start the services:
-  Start Sparkta: **sudo service spark start**
-  Start Elasticsearch: **sudo service elasticsearch start**

Now you are ready to `test <examples.html>`__ the sandbox

What you will find in the sandbox
=================================

-  OS: CentOS 6.5
-  3GB RAM - 2 CPU
-  Two ethernet interfaces.

+------------------+---------+-------------------------------+
|    Name          | Version |         Command               |
+==================+=========+===============================+
| Spark            | 1.3.0   | service spark start           |
+------------------+---------+-------------------------------+
| Cassandra        | 2.1.2   | service cassandra start       |
+------------------+---------+-------------------------------+
| MongoDB          | 2.6.9   | service mongod start          |
+------------------+---------+-------------------------------+
| Elasticsearch    | 1.5.2   | service elasticearch start    |
+------------------+---------+-------------------------------+
| zookeeper        | 2.3.6   |                               |
+------------------+---------+-------------------------------+
| Kafka            | 0.8.1   |                               |
+------------------+---------+-------------------------------+
| scala            | 2.10.4  |                               |
+------------------+---------+-------------------------------+
| RabbitMQ         | 3.5.1   | service rabbitmq-server start |
+------------------+---------+-------------------------------+


Access to the sandbox and other useful commands
===============================================

Useful commands
---------------

-  Start the sandbox: **vagrant up**
-  Shut down the sandbox: **vagrant halt**
-  In the sandbox, to exit to the host: **exit**

Accessing the sandbox
---------------------

-  Located in /install-folder
-  Run the command: **vagrant ssh**

Starting the Stratio Sparkta Shell
===============================

From the sandbox (vagrant ssh):

-  Starting the Stratio Sparkta Shell:
   **/opt/sds/sparkta/bin/run**
-  Exit the Stratio Stratio Sparkta Shell: **exit**

F.A.Q about the sandbox
=======================

I am in the same directory that I copy the Vagrant file but I have this error:

.. code:: bash

        A Vagrant environment or target machine is required to run this
        command. Run vagrant init to create a new Vagrant environment. Or,
        get an ID of a target machine from vagrant global-status to run
        this command on. A final option is to change to a directory with a
        Vagrantfile and to try again.

Make sure your file name is Vagrantfile instead of Vagrantfile.txt or
VagrantFile.

--------------

When I execute vagrant ssh I have this error:

.. code:: bash

        ssh executable not found in any directories in the %PATH% variable. Is an
        SSH client installed? Try installing Cygwin, MinGW or Git, all of which
        contain an SSH client. Or use your favorite SSH client with the following
        authentication information shown below:

We need to install `Cygwin <https://cygwin.com/install.html>`__ or `Git
for Windows <http://git-scm.com/download/win>`__.


