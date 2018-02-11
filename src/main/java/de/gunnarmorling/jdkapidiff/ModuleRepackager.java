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
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.gunnarmorling.jdkapidiff.repackager.JdkRepackager;
import japicmp.cmp.JApiCmpArchive;
import japicmp.cmp.JarArchiveComparator;
import japicmp.cmp.JarArchiveComparatorOptions;
import japicmp.config.Options;
import japicmp.model.JApiClass;
import japicmp.output.semver.SemverOut;
import japicmp.output.xml.XmlOutput;
import japicmp.output.xml.XmlOutputGenerator;
import japicmp.output.xml.XmlOutputGeneratorOptions;
import japicmp.util.Optional;

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

        @Parameter(names="--exported-packages-only",
                description=
                    "Whether only exported packages should be considered or not. Only supported if" +
                    "the two JDK versions to be compared are Java 9 or later."
        )
        private boolean exportedPackagesOnly;

        @Parameter(names="--excluded-packages")
        private String excludedPackages;
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

        JdkRepackager repackagerOld = JdkRepackager.getJdkRepackager( args.javaHome1.toPath(), args.workingDir.toPath() );
        Set<String> exported = repackagerOld.mergeJavaApi( extractedClassesDir, args.excludes1 != null ? args.excludes1 : Collections.emptyList() );

        JdkRepackager repackagerNew = JdkRepackager.getJdkRepackager( args.javaHome2.toPath(), args.workingDir.toPath() );
        exported.addAll( repackagerNew.mergeJavaApi( extractedClassesDir, args.excludes2 != null ? args.excludes2 : Collections.emptyList() ) );

        boolean exportedPackagesOnly = args.exportedPackagesOnly && repackagerOld.supportsExports() && repackagerNew.supportsExports();

        Set<String> excludedPackages = args.excludedPackages != null ? new LinkedHashSet<>( Arrays.asList( args.excludedPackages.split("\\,") ) ) : null;

        generateDiffReport( args, repackagerOld, repackagerNew, exportedPackagesOnly ? exported : null, excludedPackages );
    }

    private static void generateDiffReport(Args args, JdkRepackager oldJdk, JdkRepackager newJdk, Set<String> includedPackages, Set<String> excludedPackages) throws IOException {
        Path outputFile = args.workingDir.toPath().resolve( "jdk-api-diff.html" );

        Options options = Options.newDefault();
        options.setNoAnnotations( true );
        options.setIgnoreMissingClasses( true );
        options.setOutputOnlyModifications( true );
        options.setOldArchives( Arrays.asList( new JApiCmpArchive( oldJdk.getMergedJarPath().toFile(), oldJdk.getVersion() ) ) );
        options.setNewArchives( Arrays.asList( new JApiCmpArchive( newJdk.getMergedJarPath().toFile(), newJdk.getVersion() ) ) );

        if ( excludedPackages != null ) {
            for ( String excluded : excludedPackages ) {
                options.addExcludeFromArgument( Optional.of( excluded ), false );
            }
        }

        if ( includedPackages != null ) {
            for ( String included : includedPackages ) {
                options.addIncludeFromArgument( Optional.of( included ), true );
            }
        }

        options.setHtmlOutputFile( Optional.of( outputFile.toString() ) );

        List<JApiClass> jApiClasses = generateDiff(oldJdk, newJdk, options);
        createHtmlReport( oldJdk, newJdk, options, jApiClasses );
        cleanupOutput( outputFile, oldJdk, newJdk );
    }

    private static List<JApiClass> generateDiff(JdkRepackager oldJdk, JdkRepackager newJdk, Options options) {
        System.out.println( "Generating API diff" );

        JarArchiveComparatorOptions comparatorOptions = JarArchiveComparatorOptions.of( options );
        JarArchiveComparator jarArchiveComparator = new JarArchiveComparator( comparatorOptions );
        List<JApiClass> jApiClasses = jarArchiveComparator.compare(
                new JApiCmpArchive( oldJdk.getMergedJarPath().toFile(), oldJdk.getVersion() ),
                new JApiCmpArchive( newJdk.getMergedJarPath().toFile(), newJdk.getVersion() )
        );
        return jApiClasses;
    }

    private static void createHtmlReport(JdkRepackager oldJdk, JdkRepackager newJdk, Options options,
            List<JApiClass> jApiClasses) {
        System.out.println( "Creating HTML report" );

        SemverOut semverOut = new SemverOut( options, jApiClasses );
        XmlOutputGeneratorOptions xmlOutputGeneratorOptions = new XmlOutputGeneratorOptions();
        xmlOutputGeneratorOptions.setCreateSchemaFile( true );
        xmlOutputGeneratorOptions.setSemanticVersioningInformation( semverOut.generate() );
        xmlOutputGeneratorOptions.setTitle( "JDK " + oldJdk.getVersion() + " to " + newJdk.getVersion() + " API Change Report" );

        XmlOutputGenerator xmlGenerator = new XmlOutputGenerator( jApiClasses, options, xmlOutputGeneratorOptions );
        XmlOutput output = xmlGenerator.generate();
        XmlOutputGenerator.writeToFiles( options, output );
    }

    private static void cleanupOutput(Path outputFile, JdkRepackager oldJdk, JdkRepackager newJdk) throws IOException {
        Path outputFileTrimmed = outputFile.getParent().resolve( outputFile.getFileName() + ".new" );

        PrintWriter writer = new PrintWriter( outputFileTrimmed.toFile(), "UTF-8" );

        try ( Stream<String> stream = Files.lines( outputFile ) ) {
            stream.forEach( l -> {
                writer.write( l.replaceAll( "\\s+$", "" )
                        .replaceAll( oldJdk.getMergedJarPath().toString(), "JDK " + oldJdk.getVersion() )
                        .replaceAll( newJdk.getMergedJarPath().toString(), "JDK " + newJdk.getVersion() ) +
                        System.lineSeparator()
                );
            } );
        }

        writer.close();

        Files.move( outputFileTrimmed, outputFile, StandardCopyOption.REPLACE_EXISTING );
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
