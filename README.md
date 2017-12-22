# JDK API Diff Report Generator

This project creates a report of all API changes between two different JDK versions, e.g. JDK 8 and 9, [JapiCmp](https://github.com/siom79/japicmp).

## Published reports

Report created by this generator can be found here (excluding any unsupported Sun/Oracle/Apple modules):

* [comparing JDK 9.0.1 against JDK 1.8.0_151](https://gunnarmorling.github.io/jdkapidiff/jdk8-jdk9-api-diff.html)
(it's 16 MB, so loading may take a bit)
* [comparing JDK 10-ea (b36) against JDK 9.0.1](https://gunnarmorling.github.io/jdkapidiff/jdk9-jdk10-api-diff.html)

## Usage

To create the report yourself, e.g. with different settings, run `mvn clean install`.
The API change report can be found at _target/japicmp/japicmp.html_.
Adapt the excludes in the execution of the japicmp-maven-plugin as needed.

[Maven Toolchains](https://maven.apache.org/guides/mini/guide-using-toolchains.html) are used to locate the different JDKs.
There must be a toolchain of type `jdk` for the JDKs to compare.
Provide a file _~.m2/toolchains.xml_ like this:

```xml
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
```

Adjust the names for `oldVersion` and `newVersion` configured for the `japicmp-maven-plugin` depending on the exact JDK versions you're comparing.

If you're interested in changes in specific packages, adjust the excludes of the `japicmp-maven-plugin` as needed.

## License

This project is licensed under the Apache License version 2.0.
