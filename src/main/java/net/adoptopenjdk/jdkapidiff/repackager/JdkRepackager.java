/**
 *  Copyright 2017-2018 AdoptOpenJDK contributors (https://adoptopenjdk.net/);
 *  See the copyright.txt file in the distribution for a full listing of all
 *  contributors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.adoptopenjdk.jdkapidiff.repackager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.spi.ToolProvider;

import net.adoptopenjdk.jdkapidiff.ProcessExecutor;

public abstract class JdkRepackager {

    protected final Path javaHome;
    protected final String version;
    private final Path workingDir;

    public static JdkRepackager getJdkRepackager(Path javaHome, Path workingDir) {
        String version = getVersion( javaHome );

        if ( version.startsWith( "1.") ) {
            return new Jdk8Repackager( javaHome, version, workingDir );
        }
        else {
            return new Jdk9Repackager( javaHome, version, workingDir );
        }
    }

    protected JdkRepackager(Path javaHome, String version, Path workingDir) {
        this.javaHome = javaHome;
        this.version = version;
        this.workingDir = workingDir;
    }

    /**
     * Merges the represented JDK's classes into a single JAR for comparison
     * purposes.
     *
     * @return The packages exported by the represented JDK
     */
    public Set<String> mergeJavaApi(Path extractedClassesDir, List<String> excludes) throws IOException {
        System.out.println( "Merging JARs/modules from " + javaHome + " (version " + version + ")" );

        Path targetDir = extractedClassesDir.resolve( version );
        Files.createDirectories( targetDir );

        Set<String> exportedPackages = extractJdkClasses( targetDir );

        Path fileList = Paths.get( workingDir.toUri() ).resolve( version + "-files" );

        Files.write(
                fileList,
                getFileList( targetDir, excludes ).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE
        );

        System.out.println( "Creating " + getMergedJarPath() );

        Optional<ToolProvider> jar = ToolProvider.findFirst( "jar" );
        if ( !jar.isPresent() ) {
            throw new IllegalStateException( "Couldn't find jar tool" );
        }

        jar.get().run(
                System.out,
                System.err,
                "-cf", getMergedJarPath().toString(),
                "@" + fileList
        );

        return exportedPackages;
    }

    public Path getMergedJarPath() {
        String apiJarName = "java-" + version + "-api.jar";
        return workingDir.resolve( apiJarName );
    }

    public String getVersion() {
        return version;
    }

    /**
     * Whether the extracted JDK has a defined API represented by exports (JDK 9 and onwards) or not.
     */
    public abstract boolean supportsExports();

    protected abstract Set<String> extractJdkClasses(Path targetDir) throws IOException;

    private static String getVersion(Path javaHome) {
        List<String> output = ProcessExecutor.run( "java", Arrays.asList( javaHome.resolve( "bin" ).resolve( "java" ).toString(), "-version" ), javaHome.resolve( "bin" ) );
        String version = output.get( 0 ).contains( "JAVA_TOOL_OPTIONS" ) ? output.get( 1 ) : output.get( 0 );
        return version.substring( version.indexOf( "\"" ) + 1, version.lastIndexOf( "\"" ) );
    }

    private static String getFileList(Path java8Dir, List<String> excludes) {
        StringBuilder fileList = new StringBuilder();

        try {
            Files.walkFileTree( java8Dir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    java8Dir.relativize( file );

                    for ( String exclude : excludes ) {
                        if ( file.startsWith( exclude ) ) {
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    fileList.append( file ).append( System.lineSeparator() );

                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (IOException e) {
            throw new RuntimeException( e );
        }

        return fileList.toString();
    }
}
