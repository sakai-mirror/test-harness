The Sakai test harness provides integration testing support for sakai tools and
services.  While all projects should include unit testing, integration testing
is an important tool for ensuring that your tool or service is working properly
within a (simulated) sakai environment.

You can follow these steps add integration tests to your sakai project:

1) Create a new maven project in your module named *integration-test (e.g.
myproject-integration-test).

2) Use the project.sample.xml file as a template for setting up your build.

3) Configure your build properties in project.properties:

### REQUIRED PROPERTIES FOR INTEGRATION TESTING ###
maven.junit.fork=yes
maven.test.skip=true

### OPTIONAL PROPERTIES ###
maven.junit.format=plain
maven.junit.usefile=false
maven.junit.jvmargs=-Xms256m -Xmx256m

4) Create a src/test directory and add your JUnit tests.  Your unit test cases
should extend org.sakaiproject.test.SakaiTestBase.

*** IMPORTANT NOTE #1: If you intend to write multiple test cases (java classes
that extend org.sakaiproject.test.SakaiTestBase), please ensure that your
project.xml is configured to run a single test *suite* that runs all of your
tests.  This is important because, for each test case that maven runs directly, the
test harness will launch a new sakai component manager, which takes a long time
(around 10 seconds on my desktop).  By using a test suire to run all of your tests,
you will incur the startup delay only once.

Your test suite should call oneTimeSetup and oneTimeTearDown, like so:

	public static Test suite() {
		TestSetup setup = new TestSetup(new TestSuite(MyTest.class)) {
			protected void setUp() throws Exception {
				oneTimeSetup();
			}
			protected void tearDown() throws Exception {
				oneTimeTearDown();
			}
		};
		return setup;
	}


*** IMPORTANT NOTE #2: The test harness requires that you have a typical Sakai
development environment configured.  It expects either:
a) a 'test.tomcat.home' property pointing to a tomcat instance with all of the sakai
components deployed
b) a build.properties file in your $HOME directory, where it can find an entry pointing
to your maven.tomcat.home.

The test harness also loads your sakai.properties file in either
${maven.tomcat.home}/sakai/ or ${test.tomcat.home}/sakai.

If your sakai.properties is configured to use an oracle database, for instance, you
need to add a dependency on the appropriate oracle driver to your integration
testing project.  Understand that the data in this database will be modified by
integration tests, and failing or poorly written tests (those that don't clean up after
themselves) may leave garbage in your DB.  Using an in-memory hsql database is
recommended.

5) Integration tests run as a standalone maven goal named 'itest'.  It can only
be run on a fully built and deployed Sakai.  When running 'itest ', you must
also override the directive in the test harness that skips running these tests
during a normal build (since the tests will fail if the full deployment is not
in place).

maven -Dmaven.test.skip=false itest

--------------------------------------------------------------------------------

Josh Holtzman
jholtzman@berkeley.edu
