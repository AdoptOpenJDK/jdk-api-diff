# JDK API Diff Report Generator

This project creates a report of all API changes between two different JDK versions,
using [JapiCmp](https://github.com/siom79/japicmp).
You can use this tool for instance to compare OpenJDK 9 and OpenJDK 10, OpenJDK 9 and Oracle JDK 9 etc.

## Published reports

Example reports created by this generator can be found in the _docs_ directory:

* API comparison of OpenJDK 9.0.4 and OpenJDK 10-b46: [jdk9-jdk10-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk9-jdk10-api-diff.html)
* API comparison of OpenJDK 10-b46 and OpenJDK 11-b04: [docs/jdk10-jdk11-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk10-jdk11-api-diff.html)
(it's 13 MB, so loading may take a bit)

## Usage

To create reports yourself, e.g. with different settings, run `mvn clean install`.
The API change report can be found at _target/jdk-api-diff.html_.

[Maven Toolchains](https://maven.apache.org/guides/mini/guide-using-toolchains.html) are used to locate the JDKs to compare.
There must be a toolchain entry of type `jdk` for each JDK to compare.
Provide a file _~.m2/toolchains.xml_ like this:

```xml
<?xml version="1.0" encoding="UTF8"?>
<toolchains>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>9</version>
            <vendor>openjdk</vendor>
        </provides>
        <configuration>
            <jdkHome>/path/to/jdk-9</jdkHome>
        </configuration>
    </toolchain>
    <toolchain>
        <type>jdk</type>
        <provides>
            <version>10</version>
            <vendor>openjdk</vendor>
        </provides>
        <configuration>
            <jdkHome>/path/to/jdk-10</jdkHome>
        </configuration>
    </toolchain>
</toolchains>
```

Specify two properties, `jdk1` and `jdk2` in your _pom.xml_, identifying the base and target JDK version for the comparison.
The values are comma-separated requirements matched against the `<provides>` configurations of the existing toolchain entries.
Both properties must unambiguously identify one toolchain, for example:

```xml
<jdk1>version=9,vendor=openjdk</jdk1>
<jdk2>version=10,vendor=openjdk</jdk2>
```

If there's no matching toolchain or multiple ones match the given requirements, an exception will be raised.

The report is created via the `ModuleRepackager` class which is executed with the Maven exec plug-in.
Adjust the following options passed to that class in _pom.xml_ as needed:

* `--exported-packages-only`: `true` or `false`, depending on whether only exported packages should be compared
or all packages; only applies if both compared versions are Java 9 or later
* `--excluded-packages`: a comma-separated listed of package names which should be excluded from the comparison;
this can be useful to exclude unsupported packages such as `com.apple` or to ignore packages missing from Oracle EA builds
but present in final versions such as `jdk.management.jfr`

## License

This project is licensed under the Apache License version 2.0.
