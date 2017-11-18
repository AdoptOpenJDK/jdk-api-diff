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
package de.gunnarmorling.jdkapidiff;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.gunnarmorling.jdkapidiff.repackager.JdkRepackager;

public class ModuleRepackager {

    public static class Args {

        @Parameter(names="--javaHome1")
        private File javaHome1;

        @Parameter(names="--excludes1")
        private List<String> excludes1;

        @Parameter(names="--javaHome2")
        private File javaHome2;

        @Parameter(names="--excludes2")
        private List<String> excludes2;

        @Parameter(names="--working-dir")
        private File workingDir;
    }

    public static void main(String[] argv) throws Exception {
        Args args = new Args();
        JCommander.newBuilder()
            .acceptUnknownOptions( true )
            .addObject( args )
            .build()
            .parse( argv );

        Path extractedClassesDir = args.workingDir.toPath().resolve( "extracted-classes" );
        delete( extractedClassesDir );

        JdkRepackager repackager = JdkRepackager.getJdkRepackager( args.javaHome1.toPath() );
        repackager.mergeJavaApi( args.workingDir.toPath(), extractedClassesDir, args.excludes1 != null ? args.excludes1 : Collections.emptyList() );

        repackager = JdkRepackager.getJdkRepackager( args.javaHome2.toPath() );
        repackager.mergeJavaApi( args.workingDir.toPath(), extractedClassesDir, args.excludes2 != null ? args.excludes2 : Collections.emptyList() );
    }

    private static Path delete(Path dir) {
        try {
            if ( Files.exists( dir ) ) {
                Files.walkFileTree( dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete( file );
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete( dir );
                        return FileVisitResult.CONTINUE;
                    }
                });
            }

            Files.createDirectory( dir );
        }
        catch (IOException e) {
            throw new RuntimeException( "Couldn't recreate directory " + dir, e );
        }

        return dir;
    }
}
