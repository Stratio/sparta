Create a local image
--------------------

If you want to generate a local image you can by first creating the package of sparta.
For example in the parent directory run this script:

**NOTE: In order to create a package the system need rpmbuild (in ubuntu the package is rpm)**

```bash
mvn clean install -Ppackage -DskipUTs -DskipITs -DskipTests
```

Now change to this dir (docker/localDocker) and  run : 

```bash
./createDocker.sh

``` 

Now hopefully you have the docker image in your system. For running try :

```bash
docker run -d  -p 9090:9090 -p 9091:9091 -p 4040:4040 --name sparta sparta 
```
