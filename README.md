# JDK API Diff Report Generator

This project creates a report of all API changes between two different JDK versions, e.g. JDK 8 and 9, using [JapiCmp](https://github.com/siom79/japicmp).

## Published reports

Report created by this generator can be found here (excluding any unsupported Sun/Oracle/Apple modules):

* [comparing JDK 9.0.1 against JDK 1.8.0_151](https://gunnarmorling.github.io/jdkapidiff/jdk8-jdk9-api-diff.html)
(it's 16 MB, so loading may take a bit)
* [comparing JDK 10-ea (b42) against JDK 9.0.4](https://gunnarmorling.github.io/jdkapidiff/jdk9-jdk10-api-diff.html)

## Usage

To create the report yourself, e.g. with different settings, run `mvn clean install`.
The API change report can be found at _target/jdk-api-diff.html_.

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

Specify two properties, `jdk1` and `jdk2` in your _pom.xml_, identifying the base and target JDK version for the comparison.
The values are comma-separated requirements matched against the `<provides>` configurations of the existing toolchain entries.
Both properties must unambiguously identify one toolchain, for example:

```xml
<jdk1>version=9,vendor=oracle</jdk1>
<jdk2>version=10,vendor=oracle</jdk2>
```

If there's no matching toolchain or multiple ones match the given requirements, an exception will be raised.

The report is created via the `ModuleRepackager` class which is executed with the Maven exec plug-in.
Adjust the following options passed to that class in _pom.xml_ as needed:

* `--exported-packages-only`: `true` or `false`, depending on whether only exported packages should be compared
or all packages; only applies if both compared versions are Java 9 or later
* `--excluded-packages`: a comma-separated listed of package names which should be excluded from the comparison

## License

This project is licensed under the Apache License version 2.0.
