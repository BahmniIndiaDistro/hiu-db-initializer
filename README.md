# hiu-db-initializer

Database migartion for health-information-user.

## :rocket: Running From Source

```
mvn clean install
```

```
mvn package
```

```
java -Djdbc.url=jdbc:postgresql://${db_host}:${db_port}/${database_name} -Djdbc.username=${user_name} -Djdbc.password=${password} -jar target/hiu-db-initializer-1.0-SNAPSHOT.jar
```
