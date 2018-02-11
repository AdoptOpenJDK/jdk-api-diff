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
	else if ( toolChains.size > 1 ) {
		throw new IllegalArgumentException( "Multiple matching toolchains found for requirements " + jdkSelector );
	}
	else {
		return new java.io.File( toolChains.first().findTool( 'javac' ) ).getParentFile().getParentFile().toString()
	}
}

project.properties.javaHome1 = getJdkHome( project.properties.jdk1 );
project.properties.javaHome2 = getJdkHome( project.properties.jdk2 );
