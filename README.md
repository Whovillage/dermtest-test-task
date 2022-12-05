# Dermtest test task

This is Kevin Kesküla's solution to the Dermtest practical task. As there was no deadline imposed and I am new to Java and Spring Boot I decided to try and complete all the subtasks and also had to read up on the Java and Spring Boot ecosystem.

Project starts at port 8080.
API docs available at path /swagger

Here is how I completed the subtasks:

## Creating the project

Initialising the project with required dependencies was very straightforward with Spring Initializr.

## Add a file based database

This required some research. I wanted to use Sqlite in the beginning as this is what I have been working with before, but in the end I chose to include H2 and configure it in prod-mode to persist the database with `spring.datasource.url=jdbc:h2:file:./data/demo`. This task took about 30 minutes to complete.

## Add Lombok

Adding Lombok itself was easy using the Spring Initializr. Additionally I had to get up to speed on the scope and uses of Lombok. In this project I used the `@Data` and `@NoArgsConstructor` annotations from the library to reduce boilerplate code. Adding Lombok itself to the project took no extra time, but reading the docs added an extra 30 minutes.

## Create a runnable Docker image

With docker I used a 2-stage build where in the first step Docker builds up the project and in the second the .jar files are copied and executed. This way the final image is leaner. `target` was put in the .dockerignore as the folder in not needed for building the image. This step took about 20 minutes to complete.

## Add OpenAPI documentation

To add openAPI docs I added `springdoc-openapi-ui` as a project dependency. This was already enough to autogenerate the basic docs about the controller. The docs were not 100% correct however so I added swagger annotations to the controller methods to imporive the quality. What to import and from where and how to use the annotations was confusing, but in the end I managed to improve the quality of the API documentation. I added custom paths of `api-docs` for the JSON representation and `/swagger` for the HTML representation of the docs for convenience. This step took around an hour to complete.

## Add log4j2

To add log4j2, I added it as a dependency in `pom.xml`. In addition I added a `log4j2.xml` config file with a basic logging pattern and a logger for root and application. Loggers log to console, I did not add logging to files for this task. This step took around 20 minutes to complete.

## Add prod and dev configs

Adding two different configs required to add `application-prod.properties` and `application-dev.properties`. I struggled quite a bit to get the project to actually boot in the different modes, because I was missing the `spring.config.activate.on-profile` line from the files. The project only uses prod and dev to differentiate between database modes - dev mode uses an in-memory config of the H2 database for easier testing and prod uses a file-based config. The task took an hour to finish.

## Create a data model

I created a basic Doctor model with 4 `String` fields and an `id`. Creating the class itself took a couple minutes, but additionally adding validation, finding the correct annotations for connecting to the database, autogenerating id-s and integrating Lombok took an additional hour in aggregate.

## Creating a CRUD controller

I created a DoctorController class with GET, POST, PUT and DELETE methods. In addition I added one specific Exeptionhandler to handle bad requests. The controlled was Autowired to the database. In addition I wrote unit tests for all happy paths and common failure paths for the controller. Finding the correct tools to import, learning how to use them and then writing the controller and tests took around 3 hours to complete.

## Create a JPA repo

To create a JPA repository I added `spring-boot-starter-data-JPA` as a dependency and created a `DoctorRepository` that extends the `JpaRepository` class. Then I used `@Autowire` in the controller to connect it to `DoctorController`. This step took 30 minutes.

## Add Oauth2

I added log-in with Github using Oauth2. For this I added `spring-boot-starter-oauth2-client` as a dependency. Then I registered a new oauth app in Github and added the clientID and client secret to project configuration. This automatically added Github auth to the project. In real environnment the secret should never be visible in the remote repo, but with this test task I decided to push the secret to remote so that the evaluator would also have easy access when testing the app.

Adding security to the app, however, introduced a complication - namely CSRF protection got enabled in addition to auth. This caused all POST requests to give a 403 response. Debugging and solving this problem took me down a rabbithole for a couple of hours. I was finally able to find a way to disable CSRF by changing the WebSecurity configuration of the application. The solution uses a deprecated `WebSecurityConfigurerAdapter` to achieve it, but as this was not in the actual scope of this task I left it as is. In production environments where users are able to produce input through the browser and sensitive information is at play, CSRF should definitely stay enabled.

## Bonus

Added a basic index.html where the user can add new doctors to the database.

In total the tasks took me around 11 hours to finish. A lot of it was spent on getting familiar with the Spring Boot ecosystem. I feel like I learned a lot and I would be much quicker when starting from scratch now.
