### EXECUTING TESTS


```
It's optional to provide BASE_URL variable to the maven command.
```
The generic command is:
```
  mvn clean -Dgatling.simulationClass=com.stratio.performance.sparta.CreateWorkflowScenario:execute

$ mvn clean -P<POM_PROFILE> gatling:execute -DlogLevel=DEBUG
```
or
```
$ mvn clean -Dgatling.simulationClass=<CLASS_NAME_WITH_PACKAGE> gatling:execute -DlogLevel=DEBUG
```
And all the implemented tests are:
```

#### Examples:

mvn clean -Dgatling.simulationClass=com.stratio.performance.sparta.CreateWorkflowScenario gatling:execute
