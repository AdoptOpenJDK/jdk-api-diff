# WARN - PROJECT IS RETIRED

Please note this project has been retired in favour of [https://javaalmanac.io/jdk/21/](https://javaalmanac.io/jdk/21/).

-------











## JDK API Diff Report Generator

This project creates a report of all API changes between two different JDK versions,
using [JapiCmp](https://github.com/siom79/japicmp).
You can use this tool for instance to compare OpenJDK 9 and OpenJDK 10, OpenJDK 9 and Oracle JDK 9 etc.

## Published reports

Example reports created by this generator can be found in the _docs_ directory:

### N => N+1

* API comparison of OpenJDK 8u301 and OpenJDK 9.4.0: [jdk8-jdk9-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk8-jdk9-api-diff.html)
* API comparison of OpenJDK 9.0.4 and OpenJDK 10-b46: [jdk9-jdk10-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk9-jdk10-api-diff.html)
* API comparison of OpenJDK 10.0.2 and OpenJDK 11+28: [jdk10-jdk11-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk10-jdk11-api-diff.html)
* API comparison of OpenJDK 11.0.1+13 and OpenJDK 12-ea+27: [jdk11-jdk12-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk11-jdk12-api-diff.html)
* API comparison of OpenJDK 12.0.1 and OpenJDK 13-ea+25: [jdk12-jdk13-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk12-jdk13-api-diff.html)
* API comparison of OpenJDK 13.0.2 and OpenJDK 14.0.1: [jdk13-jdk14-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk13-jdk14-api-diff.html)
* API comparison of OpenJDK 14.0.1 and OpenJDK 15-ea+27: [jdk14-jdk15-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk14-jdk15-api-diff.html)
* API comparison of OpenJDK 15.0.2 and OpenJDK 16.0.1: [jdk15-jdk16-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk15-jdk16-api-diff.html)
* API comparison of OpenJDK 16.0.1 and OpenJDK 17+35: [jdk16-jdk17-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk16-jdk17-api-diff.html)
* API comparison of OpenJDK 17.0.0 and OpenJDK 18-ea+18-1093 [jdk17-jdk18-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk17-jdk18-api-diff.html)

### LTS => LTS+1

* API comparison of OpenJDK 8u301 and OpenJDK 11.0.11: [jdk8-jdk11-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk8-jdk11-api-diff.html)
* API comparison of OpenJDK 11.0.11 and OpenJDK 17+35: [jdk11-jdk17-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk11-jdk17-api-diff.html)

### LTS => LTS+2

* API comparison of OpenJDK 8u301 and OpenJDK 17+35: [jdk8-jdk17-api-diff.html](https://gunnarmorling.github.io/jdk-api-diff/jdk8-jdk17-api-diff.html)

Some of them are huge, so loading may take a bit.

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
