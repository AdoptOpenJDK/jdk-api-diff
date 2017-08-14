def getJdkHome(String version) {
	tm = session.lookup( 'org.apache.maven.toolchain.ToolchainManager' )
	toolChains = tm.getToolchains( session, 'jdk', [version:version] );

	if ( !toolChains.isEmpty() ) {
		return new java.io.File( toolChains.first().findTool( 'javac' ) ).getParentFile().getParentFile().toString()
	}

	return null;
}

jdk8Home = getJdkHome('1.8')
if ( jdk8Home != null ) {
	project.properties.java8home = jdk8Home
}
else {
	throw new IllegalArgumentException( "No toolchain found for JDK 1.8" )
}

jdk9Home = getJdkHome('9')
if ( jdk9Home != null ) {
	project.properties.java9home = jdk9Home
} 
else {
	throw new IllegalArgumentException( "No toolchain found for JDK 9" )
}
