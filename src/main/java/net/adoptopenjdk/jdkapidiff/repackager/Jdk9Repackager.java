/**
 *  Copyright 2017-2018 AdoptOpenJDK contributors (https://adoptopenjdk.net/);
 *  See the copyright.txt file in the distribution for a full listing of all
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;

class Jdk9Repackager extends JdkRepackager {

    public Jdk9Repackager(Path javaHome, String version, Path workingDir) {
        super( javaHome, version, workingDir );
    }

    @Override
    public SortedSet<String> extractJdkClasses(Path targetDir) throws IOException {
        Optional<ToolProvider> jmod = ToolProvider.findFirst( "jmod" );
        if ( !jmod.isPresent() ) {
            throw new IllegalStateException( "Couldn't find jmod tool" );
        }

        ExportsRetrievingOutputStream exportsRetriever = new ExportsRetrievingOutputStream();

        Files.list( javaHome.resolve( "jmods" ) )
            .filter( p -> !p.getFileName().toString().startsWith( "jdk.internal") )
            .forEach( module -> {
                System.out.println( "Extracting module " + module );
                jmod.get().run( System.out, System.err, "extract", "--dir", targetDir.toString(), module.toString() );
                jmod.get().run( exportsRetriever, new PrintWriter( System.err ), "describe", module.toString() );
            });

        Files.delete( targetDir.resolve( "classes" ).resolve( "module-info.class") );

        return new TreeSet<>( exportsRetriever.getExports() );
    }

    @Override
    public boolean supportsExports() {
        return true;
    }

    private static class ExportsRetrievingOutputStream extends PrintWriter {

        // TODO handle qualified exports; currently there seem to be none
        private static final Pattern EXPORTS_PATTERN = Pattern.compile("exports (.*)");

        private List<String> exports = new ArrayList<>();

        public ExportsRetrievingOutputStream() {
            super( new ByteArrayOutputStream() );
        }

        @Override
        public void println(String x) {
            try(Scanner lines = new Scanner(x)) {
                while(lines.hasNext()) {
                    Matcher matcher = EXPORTS_PATTERN.matcher( lines.nextLine() );

                    if ( matcher.matches() ) {
                        exports.add( matcher.group( 1 ) );
                    }
                }
            }
        }

        public List<String> getExports() {
            return exports;
        }
    }
}
