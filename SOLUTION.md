### The solution to the task has been developed on a springboot application framework.

### System Requirements

* Install Java JDK 17 on your development machine (https://www.oracle.com/java/technologies/downloads/)

* Have maven installed on your development system and available in system path check
  out https://maven.apache.org/install.html

### Running the application

* Clone this repository
* then run the command
  ```mvn install``` from the project root directory to get the dependencies from maven central

* run ```mvn spring-boot:run``` to run the application, then you can use `postman` to communicate with the `REST API`

### Running Tests

* to run the tests, use ```mvn test``` command from the project root folder

### Building the Application

* to build the application, use ```mvn clean package``` command to generate the fat jar of the application. The fat jar
  will be located in the `target` folder within the root of the application folder named `drone-task-0.1`

#### Other information

The application uses `h2 in-memory` database. The database tables are created at runtime and populated with sample data
by the `PopulateSampleData` util service. This can be disabled from the `application.properties` file by setting
the `sample.data.populate` value to `false`.
<br>

### Application Endpoint

The OpenAPI documentation is available at `http://localhost:8080/swagger-ui/index.html`
  

