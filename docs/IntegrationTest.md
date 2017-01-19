#Integration Test 

When running integration test in the CI server , 
the CI server will run all docker images in the ```Jenkinsfile```  
defined at ```ITSERVICES```


###Docker Containers in local machine

####Rabbitmq

There are multiple options when running in local machine.

- Stratio qa image (in the CI server this image will be displayed)

```bash
docker run -d -i -p 5672:5672 --name rabbitmq qa.stratio.com/rabbitmq:3-management
```

- Rabbitmq image 

```bash
docker run -d -i -p 5672:5672 --name rabbitmq rabbitmq:3.6.6  
```