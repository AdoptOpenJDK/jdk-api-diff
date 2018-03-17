/**
 *  Copyright 2017 Gunnar Morling (http://www.gunnarmorling.de/)
 *  and/or other contributors as indicated by the @authors tag. See the
 *  copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.adoptopenjdk.jdkapidiff.repackager;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import net.adoptopenjdk.jdkapidiff.ProcessExecutor;

public class Jdk8Repackager extends JdkRepackager {

    public Jdk8Repackager(Path javaHome, String version, Path workingDir) {
        super( javaHome, version, workingDir );
    }

    @Override
    public SortedSet<String> extractJdkClasses(Path targetDir) {
        // Using separate process for using specific target directory
        Path rtJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "rt.jar" );
        System.out.println( "Extracting rt.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", rtJar.toString() ), targetDir );

        Path javawsJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "javaws.jar" );
        System.out.println( "Extracting javaws.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", javawsJar.toString() ), targetDir );

        Path jfxrtJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "ext" ).resolve( "jfxrt.jar" );
        System.out.println( "Extracting jfxrt.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", jfxrtJar.toString() ), targetDir );

        Path nashornJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "ext" ).resolve( "nashorn.jar" );
        System.out.println( "Extracting nashorn.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", nashornJar.toString() ), targetDir );

        Path jceJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "jce.jar" );
        System.out.println( "Extracting jce.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", jceJar.toString() ), targetDir );

        Path jfrJar = javaHome.resolve( "jre" ).resolve( "lib" ).resolve( "jfr.jar" );
        System.out.println( "Extracting jfr.jar" );
        ProcessExecutor.run( "jar", Arrays.asList( "jar", "-xf", jfrJar.toString() ), targetDir );

        return new TreeSet<>();
    }

    @Override
    public boolean supportsExports() {
        return false;
    }
}
