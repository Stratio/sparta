var findAllByGroupDto = {
  path: '/workflows/findAllByGroupDto/:executionId',
  template: [{"id":"eb18b826-9ec6-42e8-a66b-8093b8dea8a6","ciCdLabel":"Released","name":"workflow-name","description":"","settings":{"executionMode":"marathon","parametersLists":["Environment"],"parametersUsed":["Global.DEFAULT_DELIMITER","Global.SPARK_CORES_MAX","Global.SPARK_DRIVER_CORES","Global.SPARK_DRIVER_JAVA_OPTIONS","Global.SPARK_DRIVER_MEMORY","Global.SPARK_EXECUTOR_CORES","Global.SPARK_EXECUTOR_EXTRA_JAVA_OPTIONS","Global.SPARK_EXECUTOR_MEMORY","Global.SPARK_LOCALITY_WAIT","Global.SPARK_LOCAL_PATH","Global.SPARK_MEMORY_FRACTION","Global.SPARK_STREAMING_BLOCK_INTERVAL","Global.SPARK_STREAMING_CHECKPOINT_PATH","Global.SPARK_STREAMING_WINDOW","Global.SPARK_TASK_MAX_FAILURES"]},"nodes":[{"name":"Avro","stepType":"Input"},{"name":"Text","stepType":"Output"}],"executionEngine":"Batch","version":0,"group":"/home"}]
};

module.exports = [findAllByGroupDto];
