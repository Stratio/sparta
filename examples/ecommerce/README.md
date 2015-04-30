sudo service rabbitmq-server start


mvn -PorderLines clean install benerator:generate
mvn -PvisitLog clean install benerator:generate