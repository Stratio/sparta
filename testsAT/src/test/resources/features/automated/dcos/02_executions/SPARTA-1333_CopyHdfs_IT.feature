@rest
Feature: [QATM-1863] E2E Execution CopyHDFS

  #*********************************
  # Copy file to HDFS              *
  #*********************************
  Scenario:[QATM-1863][17] Connect to HDFS
    When I open a ssh connection to '${DCOS_CLI_HOST}' with user '${BOOTSTRAP_USER:-operador}' using pem file '${BOOTSTRAP_PEM:-src/test/resources/credentials/key.pem}'
    Given in less than '600' seconds, checking each '20' seconds, the command output 'dcos task | grep -w hdfscli' contains 'hdfscli'
    #Get ip in marathon
    When I run 'dcos task hdfscli | awk '{print $2}' | tail -1' in the ssh connection and save the value in environment variable 'ipHDF'
    #Obtent docker
    When I run 'sudo docker ps -q |sudo xargs -n 1 docker inspect --format '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}} {{ .Name }}' | sed 's/ \// /'| grep !{ipHDF} | awk '{print $2}'' in the ssh connection and save the value in environment variable 'hdfsDocker'

  @manual
  Scenario:[QATM-1863][01] Copy CSV to HDFS
    #Copy Certificates to ssl conexion
      #Compress Files
    And I run 'tar -czvf csv.tar ~/Descargas/csv' in the ssh connection
    And I run 'sshpass -p "${ROOT_PASSWORD:-stratio}" scp ~/Descargas/csv.tar root@!{ipHDF}:/tmp' locally
    And I run 'sshpass -p "stratio" scp ${DIRECTORY:-/home/diegomartinez/Descargas/csv/generali}/sie_stat_corporativas.csv root@!{ipHDF}:/tmp/csv/generali' locally
    Given I open a ssh connection to '!{ipHDF}' with user 'root' and password 'stratio'
    And I run 'sudo docker cp /tmp/csv.tar !{hdfsDocker}:/tmp' in the ssh connection
    #Uncompress Files
    And I run 'tar -xvf csv.tar' in the ssh connection
    And I run ' /hadoop-2.7.2/bin/hdfs dfs -mkdir /user/sparta-server/csv' in the ssh connection
    And I run '/hadoop-2.7.2/bin/hdfs dfs -put /tmp/csv/* /user/sparta-server/csv' in the ssh connection

  @manual
  Scenario:[QATM-1863][02] Copy Binary file to HDFS
    #Copy binaryfile to ssl conexion
    When I run 'scp  ${url_hdfs:-~/Descargas/test.binary} user@10.130.15.113:/tmp' locally
    Given I open a ssh connection to '!{ipHDF}' with user 'root' and password 'stratio'
    And I run 'sudo docker cp /tmp/test.binary !{hdfsDocker}:/tmp' in the ssh connection
    And I run ' /hadoop-2.7.2/bin/hdfs dfs -mkdir /user/sparta-server/binary' in the ssh connection
    And I run '/hadoop-2.7.2/bin/hdfs dfs -put /tmp/test.binary /user/sparta-server/binary' in the ssh connection



