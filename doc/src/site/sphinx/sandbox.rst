Stratio Deep sandbox and demo
*****************************

Vagrant Setup
=============

To get an operating virtual machine with Stratio Deep distribution up
and running, we use `Vagrant <https://www.vagrantup.com/>`__.

-  Download and install
   `Vagrant <https://www.vagrantup.com/downloads.html>`__.
-  Download and install
   `Virtumvn sialBox <https://www.virtualbox.org/wiki/Downloads>`__.
-  If you are in a windows machine, we will install
   `Cygwin <https://cygwin.com/install.html>`__.

Running the sandbox
===================

-  Initialize the current directory from the command line:
   **vagrant init stratio/deep-spark**.
-  Start the sandbox from command line: **vagrant up**

Please, be patient the first time it runs!!

Login into the sandbox as **vagrant user** and start the services:
-  Start Spark and Stratio Deep: **sudo service spark start**
-  Start Cassandra: **sudo service cassandra start**

What you will find in the sandbox
=================================

-  OS: CentOS 6.5
-  6GB RAM - 2 CPU
-  Two ethernet interfaces.

+------------+----------+-----------+-------------------------+
|    Name    | Version  |  Service  |         Command         |
+============+==========+===========+=========================+
| Spark      | 1.2.0    | spark     | service spark start     |
+------------+----------+-----------+-------------------------+
| Cassandra  | 2.1.05   |cassandra  | service cassandra start |
+------------+----------+-----------+-------------------------+
| MongoDB    | 2.6.5    | mongod    | service mongod start    |
+------------+----------+-----------+-------------------------+

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

Starting the Stratio Deep Shell
===============================

From the sandbox (vagrant ssh):

-  Starting the Stratio Deep Shell:
   **/opt/sds/spark/bin/stratio-deep-shell**
-  Exit the Stratio Stratio Deep Shell: **exit**

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

Stratio Deep Demos
==================

Demo #1: Using Spark and Cassandra
----------------------------------

This tutorial shows how Stratio Deep can be used to perform simple to
complex queries and calculations on data stored in a Cassandra cluster.
You will learn:

-  How to use the Stratio Deep interactive shell.
-  How to create a RDD from Cassandra and perform operations on the
   data.
-  How to write data from a RDD to Cassandra.

Using the Deep Shell
^^^^^^^^^^^^^^^^^^^^

The Stratio Deep shell provides a Scala interpreter that allows for
interactive calculations on Cassandra RDDs. In this section, you are
going to learn how to create RDDs of the Cassandra dataset we imported
in the previous section and how to make basic operations on them. Start
the shell:

.. code:: bash

    $ /opt/sds/spark/bin/stratio-deep-shell

Step 1: Creating an RDD
^^^^^^^^^^^^^^^^^^^^^^^

When using the Stratio Deep shell, a deepContext object has been created
already and is available for use. The deepContext is created from the
SparkContext and tells Stratio Deep how to access the cluster. However
the RDD needs more information to access Cassandra data such as the
keyspace and table names. By default, the RDD will try to connect to
“localhost” on port “9160”, this can be overridden by setting the host
and port properties of the configuration object: Define a configuration
object for the RDD that contains the connection string for Cassandra,
namely the keyspace and the table name:

.. code:: bash

    scala> val config : CassandraDeepJobConfig[Cells] = CassandraConfigFactory.create().host("localhost").rpcPort(9160).keyspace("crawler").table("Page").initialize

Create an RDD in the Deep context using the configuration object:

.. code:: bash

    scala> val rdd: RDD[Cells] = deepContext.createRDD(config)

Step 2: Filtering Data
^^^^^^^^^^^^^^^^^^^^^^

The CassandraRDD class provides a filter method that returns a new RDD
containing only the elements that satisfy a predicate. We will use it to
obtain a RDD with pages from domains containing the “abc.es” string:

.. code:: bash

    scala> val containsAbcRDD = rdd filter {c :Cells => c.getCellByName("domainName").getCellValue.asInstanceOf[String].contains("abc.es") }

Count the number of rows in the resulting object:

.. code:: bash

    scala> containsAbcRDD.count

Step 3: Caching Data
^^^^^^^^^^^^^^^^^^^^

The RDD class, extended by CassandraRDD, provides a straightforward
method for caching:

.. code:: bash

    scala> val containsAbcCached = containsAbcRDD.cache

In turn, cached RDD can be filtered the same way it is done on
non-cached RDDs. In this case, the content of the RDD is filtered on the
“responseCode” column:

.. code:: bash

    scala> val responseOkCached = containsAbcCached filter { c:Cells => c.getCellByName("responseCode").getCellValue == java.math.BigInteger.valueOf(200) }

Step 4: Grouping Data
^^^^^^^^^^^^^^^^^^^^^

A two steps method can be used to group data. Firstly the data is
transformed into a list of key-value pairs and then grouped by key.
Transformation into key-value pairs:

.. code:: bash

    scala> val byDomainPairs = rdd map { c:Cells => (c.getCellByName("domainName").getCellValue.asInstanceOf[String], c) }

Grouping by domain name:

.. code:: bash

    scala> val domainsGroupedByKey = byDomainPairs.groupByKey

Count the number of pages for each domain:

.. code:: bash

    scala> val numPagePerDomainPairs = domainsGroupedByKey map { t:(String, Iterable[Cells]) => ( t._1, t._2.size ) }

Step 5: Writing the results to Cassandra
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

From the previous step we have a RDD object “numPagePerDomainPairs” that
contains pairs of domain name (String) and the number of pages for that
domain (Integer). To write this result to the listdomains table, we will
need a configuration that binds the RDD to the given table and then
write its content to Cassandra using that configuration. The first step
is to get valid objects to write to Cassandra: cells. Cassandra cells
for populating the “listdomains” table are obtained by applying a
transformation function to the tuples of the CassandraRDD object
“numPagePerDomainPairs” to construct the cells:

.. code:: bash

    scala> val outputRDD: RDD[Cells] = numPagePerDomainPairs map {
          t: (String, Int) =>
            val c1 = Cell.create("domain", t._1, true, false);
            val c2 = Cell.create("num_pages", t._2);
            new Cells("crawler", c1, c2)
        }

Now that we have a RDD of cells to be written, we create the new
configuration for the listdomains table:

.. code:: bash

    scala> val outputConfig = CassandraConfigFactory.createWriteConfig().host("localhost").rpcPort(9160).keyspace("crawler").table("listdomains").createTableOnWrite(true).initialize

Then write the outRDD to Cassandra:

.. code:: bash

    scala> DeepSparkContext.saveRDD(outputRDD, outputConfig)

To check that the data has been correctly written to Cassandra, exit the
Deep shell, open a CQL shell and look at the contents of the
“listdomains” table:

.. code:: bash

    $ cqlsh
    cqlsh> use crawler;
    cqlsh:crawler> select * from listdomains;
