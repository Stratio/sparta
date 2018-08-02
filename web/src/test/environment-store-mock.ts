/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */

export const ENVIRONMENT_STORE_MOCK = {
   environment: {
      environment: {
         environmentList: {
            variables: [
               {
                  name: 'CASSANDRA_CLUSTER',
                  value: 'sparta'
               },
               {
                  name: 'CASSANDRA_HOST',
                  value: 'localhost'
               },
               {
                  name: 'CASSANDRA_KEYSPACE',
                  value: 'sparta'
               },
               {
                  name: 'CASSANDRA_PORT',
                  value: '9042'
               },
               {
                  name: 'CROSSDATA_ZOOKEEPER_CONNECTION',
                  value: 'localhost:2181'
               },
               {
                  name: 'CROSSDATA_ZOOKEEPER_PATH',
                  value: '/crossdata/offsets'
               },
               {
                  name: 'DEFAULT_DELIMITER',
                  value: ','
               },
               {
                  name: 'DEFAULT_OUTPUT_FIELD',
                  value: 'raw'
               },
               {
                  name: 'ES_CLUSTER',
                  value: 'elasticsearch'
               },
               {
                  name: 'ES_HOST',
                  value: 'localhost'
               },
               {
                  name: 'ES_INDEX_MAPPING',
                  value: 'sparta'
               },
               {
                  name: 'ES_PORT',
                  value: '9200'
               },
               {
                  name: 'JDBC_DRIVER',
                  value: 'org.postgresql.Driver'
               }
            ]
         },
         environmentFilter: ''
      }
   }
};
