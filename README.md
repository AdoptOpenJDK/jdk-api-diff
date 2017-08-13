# Java 8 - 9 API Diff Report Generator

This project creates a report of all API changes between Java 8 and 9, using JapiCmp.

## Usage

Run `mvn clean install`. The API change report can be found at _target/japicmp/japicmp.html_.
Adapt the excludes in the execution of the japicmp-maven-plugin as needed.

## License

This project is licensed under the Apache License version 2.0.
