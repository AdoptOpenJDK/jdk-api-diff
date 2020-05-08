/**
 *  Copyright 2017-2020 AdoptOpenJDK contributors (https://adoptopenjdk.net/);
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
def getJdkHome(String jdkSelector) {
	tm = session.lookup( 'org.apache.maven.toolchain.ToolchainManager' )

	requirements = new HashMap<>();
	for( String requirement : jdkSelector.split( "\\," ) ) {
		parts = requirement.split( "=" );
		requirements.put( parts[0], parts[1] );
	}

	toolChains = tm.getToolchains( session, 'jdk', requirements );

	if ( toolChains.isEmpty() ) {
		throw new IllegalArgumentException( "No matching toolchain found for requirements " + jdkSelector );
	}
	else if ( toolChains.size() > 1 ) {
		throw new IllegalArgumentException( "Multiple matching toolchains found for requirements " + jdkSelector );
	}
	else {
		return new java.io.File( toolChains.first().findTool( 'javac' ) ).getParentFile().getParentFile().toString()
	}
}

project.properties.javaHome1 = getJdkHome( project.properties.jdk1 );
project.properties.javaHome2 = getJdkHome( project.properties.jdk2 );
