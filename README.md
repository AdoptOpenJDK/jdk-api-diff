# Java 8 - 9 API Diff Report Generator

This project creates a report of all API changes between Java 8 and 9, using [JapiCmp](https://github.com/siom79/japicmp).

## Published report

A report created by this generator (based on Java 9 b181) can be found at https://gunnarmorling.github.io/jdkapidiff/jdk8-jdk9-api-diff.html (16 MB, so loading may take a bit).

## Usage

To create the report yourself, e.g. with different settings, run `mvn clean install`.
The API change report can be found at _target/japicmp/japicmp.html_.
Adapt the excludes in the execution of the japicmp-maven-plugin as needed.

Maven Toolchains are used to locate the different JDKs.
There must be a toolchain for Java 1.8 and one for Java 9.
Provide a file _~.m2/toolchains.xml_ like this:

    <?xml version="1.0" encoding="UTF8"?>
    <toolchains>
        <toolchain>
            <type>jdk</type>
            <provides>
                <version>1.8</version>
                <vendor>oracle</vendor>
            </provides>
            <configuration>
                <jdkHome>/path/to/jdk-1.8</jdkHome>
            </configuration>
        </toolchain>
        <toolchain>
            <type>jdk</type>
            <provides>
                <version>9</version>
                <vendor>oracle</vendor>
            </provides>
            <configuration>
                <jdkHome>/path/to/jdk-9</jdkHome>
            </configuration>
        </toolchain>
    </toolchains>

## License

This project is licensed under the Apache License version 2.0.
