FROM adoptopenjdk/openjdk9:x86_64-ubuntu-jdk-9.0.4.11-slim

ENV JAVA_TOOL_OPTIONS=""

RUN apt-get update && apt-get install -y maven git curl unzip zip

RUN curl -s http://get.sdkman.io | bash

ARG JDK1
RUN test -n "$JDK1" || (echo "JDK1 build-arg not provided - choose one from:" && bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk l java" && false)
ARG JDK2
RUN test -n "$JDK2" || (echo "JDK2 build-arg not provided - choose one from:" && bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk l java" && false)

RUN bash -c "source $HOME/.sdkman/bin/sdkman-init.sh && sdk install java $JDK1 && sdk install java $JDK2"

RUN echo '<?xml version="1.0" encoding="UTF-8"?>' > /usr/share/maven/conf/toolchains.xml
RUN echo '<toolchains xmlns="http://maven.apache.org/TOOLCHAINS/1.1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/TOOLCHAINS/1.1.0 http://maven.apache.org/xsd/toolchains-1.1.0.xsd">' >> /usr/share/maven/conf/toolchains.xml
RUN echo "<toolchain><type>jdk</type><provides><version>10</version><vendor>openjdk</vendor></provides><configuration><jdkHome>$HOME/.sdkman/candidates/java/$JDK1</jdkHome></configuration></toolchain>" >> /usr/share/maven/conf/toolchains.xml
RUN echo "<toolchain><type>jdk</type><provides><version>11</version><vendor>openjdk</vendor></provides><configuration><jdkHome>$HOME/.sdkman/candidates/java/$JDK2</jdkHome></configuration></toolchain>" >> /usr/share/maven/conf/toolchains.xml
RUN echo '</toolchains>' >> /usr/share/maven/conf/toolchains.xml

COPY /src /jdk-api-diff/src/
COPY pom.xml /jdk-api-diff/pom.xml
COPY LICENSE.txt /jdk-api-diff/LICENSE.txt

WORKDIR jdk-api-diff

RUN mvn install -Djdk1=version=1,vendor=openjdk -Djdk2=version=2,vendor=openjdk