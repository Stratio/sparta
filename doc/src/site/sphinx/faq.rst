FAQs
=====

**I'm in the website and I'm trying to create an input or output but the "ADD NEW" button doesn't take effect.**

- This is because Zookeeper is not running. If you don't have Zookeeper installed you can download it |zookeeper_dl|, and here is |zookeeper_run|

  .. |zookeeper_dl| raw:: html

   <a href="https://zookeeper.apache.org/releases.html#download/"
   target="_blank">here</a>


  .. |zookeeper_run| raw:: html

   <a href="https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html"
   target="_blank"> the execution guide.</a>

If it's already installed, to run Zookeeper just::

    bin/zkServer.sh start

**I have a policy with a Cassandra Output, but the name of the table doesn't seem completed**

- This is probably because the Cassandra Table name is too long. Actually the name of the table is generated with the name of the cube's dimensions. The combination of these dimensions names has to be less than 48 characters and if its more, it will split at 48 characters.

**Sparkta seems to be working right but my data it's not being aggregated**

- The "timeAvailavility" might be wrong. If the size of the window is not big enough, your data might not be fitting in it. So you have to check this parameter value and also the timestamp of the event.

**I'm trying to run two or more policies at the same time in standalone mode and I got an error**

- Actually in standalone mode, Spark just allow you to have one spark context running at the same time.

**I'm using Apache Kafka as an input source, I'm sending data in json format but the parser is not working**

- Probably this is because you are sending the JSON through kafka in the wrong format, you have to send the JSON in one line format.

**I'm checking the database that I'm using and only has the last event. The other data seems to be deleted. Why?**

- If the time between your first data aggregation to the last it's quiet big, it's possible that the checkpoint value is not big enough to process the old data, so it will be deleted because Sparkta can't find it. You will only see the new aggregations.

**I'm trying to delete an input/output and I can't. Why?**

- You can't delete inputs or outputs if the policy is running. Also you can't delete outputs that are included in policies, even if they are stopped.

**I don't understand where the name of the database and the collections are coming from**

- For the supported outputs the name of the collections are formed with the name of the dimensions of the cube. The name of the database/keyspace is created from one of the output parameter in the policy.