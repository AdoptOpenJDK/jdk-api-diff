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
package net.adoptopenjdk.jdkapidiff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes a specified external processor, logging output to the given logger.
 *
 * @author Gunnar Morling
 */
public class ProcessExecutor {

    public static List<String> run(String name, List<String> command, Path workingDirectory) {
        ProcessBuilder builder = new ProcessBuilder( command ).directory( workingDirectory.toFile() );

        Process process;
        List<String> outputLines = new ArrayList<>();
        try {
            process = builder.start();

            BufferedReader in = new BufferedReader( new InputStreamReader( process.getInputStream() ) );
            String line;
            while ( ( line = in.readLine() ) != null ) {
                outputLines.add( line );
            }

            BufferedReader err = new BufferedReader( new InputStreamReader( process.getErrorStream() ) );
            while ( ( line = err.readLine() ) != null ) {
                outputLines.add( "Error: " + line );
            }

            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            System.out.println( outputLines);
            throw new RuntimeException( "Couldn't run " + name, e );
        }

        if ( process.exitValue() != 0 ) {
            System.out.println( outputLines);
            throw new RuntimeException( "Execution of " + name + " failed" );
        }

        return outputLines;
    }
}
