Maven Project for SANSA using Spark
=============================

This is a [Maven](https://maven.apache.org/)  generate a [SANSA](https://github.com/SANSA-Stack)-KGML project using [Apache Spark](http://spark.apache.org/).

Installation:
----------

```
git clone https://github.com/white-hat-of-github/SANSA-KGML.git
cd SANSA-KGML

mvn clean package
````

### Installing wordNet database
##### In the root directory of the project creat a folder with name "link" then run:
```
sbt download-database
```


