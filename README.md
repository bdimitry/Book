# Getting Started

This project is simple simulation of REST service and it's evolution made by 1 student.


## Project Scheme
![](https://github.com/bdimitry/Cat/blob/810473ebbd81d2cf56431d194c2d7fff12969407/src/main/resources/Project%20Scheme.png)

### File links

* [Initdb1.sql](src/main/resources/Initdb1.sql)
* [Initdb2.sql](src/main/resources/Initdb2.sql)
* [Rsql Logic](src/main/resources/rsqlLogic)

### Version info

* V1 In Memory save
* V2 In DB save
* V3 In DB save with Hibernate Usage
* V4 JsonB and S3 for image

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.1.3/maven-plugin/reference/htmlsingle/)
* [Spring Web](https://spring.io/projects/spring-ws)

### Guides

The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the
parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

