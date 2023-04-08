# Java Reactor MongoDB helper

## Goal
***
<p>Provide a quick (but complex) features to manage your documents in a MongoDB.</p>
<p>This project was born when I have to make complex update, commonly when I have to check if it's a creation or an update.</p>
<p>So I thought about these utilities to do that kind of stuff and with the less connection necessary.</p>

***Enjoy !***

## Technologies
***
This library use :
- Java 11 language
- Spring framework
- Spring Reactive library [io.projectreactor](https://projectreactor.io/docs)
- Based on MongoDB shell to run command ([documentation](https://www.mongodb.com/docs/manual/reference/command/))
- Sl4j to log activities
- [Lombok](https://projectlombok.org/features/) to have a more readable / maintainable code

Whole technologies are detailed in [technology documentation](docs/TECHNO.md).

## Participating
***

## Requirements
Your application should have some requirements :
- use Java
- Have a MongoDB v5.0+ (sharding db is proof)
- use [io.projectreactor](https://projectreactor.io/docs)

## Use it
***
If you want to use this library to manage your data in a MongoDB in a Java Reactive Application, you have to do this requirement :
1. Add this dependency in your pom.xml :
```xml
<dependency>
  <groupId>gaelCharlot</groupId>
  <artifactId>java-reactor-mongo-helper</artifactId>
  <version>{Latest version}</version>
</dependency>
```
2. Run `mvn install`
3. Import the utilities that this library offer to you and use available function (don't hesitate to read [documentation](docs/javadoc))

## Maintainers
***
- Gaël CHARLOT : gcharlot08140@gmail.com