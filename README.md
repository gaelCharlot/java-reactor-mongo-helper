# Java Reactor MongoDB helper

## Goal
Provide quick (but complex) features to manage your documents in MongoDB.

This project was born when I had to make complex updates, commonly when I had to check if it's a creation or an update.

So I thought about these utilities to do that kind of stuff with the least number of connections necessary.

***Enjoy !***

## Technologies
This library uses :
- Java 11 language
- Spring framework
- Spring Reactive library [io.projectreactor](https://projectreactor.io/docs)
- Based on MongoDB shell to run command ([documentation](https://www.mongodb.com/docs/manual/reference/command/))
- Sl4j to log activities
- [Lombok](https://projectlombok.org/features/) to have a more readable / maintainable code

Whole technologies are detailed in [technology documentation](docs/TECHNO.md).

## Participating
To add the Java Reactor MongoDB Helper library to your GitHub project, you can follow these steps:

1. Fork the repository from [github.com/java-reactor-mongo-helper](https://github.com/java-reactor-mongo-helper) by clicking on the "Fork" button in the upper right corner of the repository page. This will create a copy of the repository under your GitHub account.

2. Clone the forked repository to your local machine using the following command in your terminal:

```bash
git clone https://github.com/your-username/java-reactor-mongo-helper.git
```

Replace `your-username` with your GitHub username.

3. Add the cloned repository as a submodule in your own project's repository. Navigate to your project's directory and use the following command:

```bash
git submodule add https://github.com/your-username/java-reactor-mongo-helper.git path/to/submodule
```

Replace \`your-username\` with your GitHub username and \`path/to/submodule\` with the desired path where you want to add the submodule in your project.

4. Commit the changes to your own project's repository:

```bash
git add path/to/submodule
git commit -m "Add Java Reactor MongoDB Helper library as submodule"
git push
```

5. Update the submodule in your project whenever there are changes in the library's repository:
```bash
cd path/to/submodule
git pull
```

6. If you make changes to the library and want to contribute back to the original repository, you can create a pull request from your forked repository to the original repository at [github.com/java-reactor-mongo-helper](https://github.com/java-reactor-mongo-helper). Once the pull request is reviewed and approved, the changes will be merged into the library's repository.

Note: As you are the only maintainer/owner of the repository, you can directly push changes to the library's repository without creating a pull request if you prefer. However, creating a pull request allows for a review process and ensures that changes are properly reviewed before merging into the main repository.


## Requirements
Your application should have the following requirements:
- Use Java
- Have a MongoDB v5.0+ (sharding db is proof)
- Use [io.projectreactor](https://projectreactor.io/docs)

## Use it
If you want to use this library to manage your data in a MongoDB in a Java Reactive Application, you need to follow these steps:
1. Add this dependency in your pom.xml :
```xml
<dependency>
  <groupId>gaelCharlot</groupId>
  <artifactId>java-reactor-mongo-helper</artifactId>
  <version>{Latest version}</version>
</dependency>
```
2. Run `mvn install`
3. Import the utilities that this library offers to you and use the available functions (refer to the [documentation](http://www.mongohelper.documentation/javadoc/) for details).

_NB : Replace `{Latest version}` with the latest version of the library._

## Maintainers
This repository is currently maintained by [Gaël CHARLOT](https://github.com/gaelCharlot).

If you have any questions, concerns, or issues related to this repository, please feel free to put your comment in the [available discussion](https://github.com/gaelCharlot/java-reactor-mongo-helper/discussions/1) or to contact me at [gcharlot08140@gmail.com]() or create an issue on the repository's GitHub page.
