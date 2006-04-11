The Sakai test harness provides integration testing support for sakai tools and
services.  While all projects should include unit testing, integration testing
is an important tool for ensuring that your tool or service is working properly
within a (simulated) sakai environment.

You can follow these steps add integration tests to your sakai project:

1) Create a new maven project in your module named *integration-test (e.g.
legacy-integration-test).

2) Use the project.sample.xml file as a template for setting up your build.

3) Configure any values you prefer to use in project.properties.  I like:

# JUnit formatting (plain gives more details than brief)
maven.junit.format=plain

# Display junit output rather than redirecting it to a file
maven.junit.usefile=false

4) Create a src/test directory and add your JUnit tests.  Your unit test cases
should extend org.sakaiproject.test.SakaiTestBase.  Use the integration tests in
test-harness/src/test as examples.

*** IMPORTANT NOTE #1: If you intend to write multiple test cases (java classes
that extend org.sakaiproject.test.SakaiTestBase), please ensure that your
project.xml is configured to run a single test *suite* that runs all of your
tests.  See  org.sakaiproject.test.SakaiIntegrationTest for an example of a test
suite that runs tests from a variety of test cases.  This is important because,
for each test case that maven runs directly, the test harness will launch a new
sakai component manager, which takes a long time (around 10 seconds on my
desktop).  By using a test suire to run all of your tests, you will incur the
startup delay only once.

*** IMPORTANT NOTE #2: The test harness requires that you have a typical Sakai
development environment configured.  It expects a build.properties file in your
$HOME directory, and it expects an entry pointing to your maven.tomcat.home.  It
also loads your sakai.properties file in maven.tomcat.home/sakai/.  So, if your
sakai.properties is configured to use an oracle database, for instance, you need
to add a dependency on the appropriate oracle driver to your integration testing
project.  You should also not care about the data in this database, since
failing or poorly written tests (those that don't clean up after themselves) may
leave garbage data in your DB.  Using an in-memory hsql database is recommended.

5) Integration tests run as a standalone maven goal named 'itest'.  It can only
be run on a fully built and deployed Sakai.  When running 'itest ', you must
also override the directive in the test harness that skips running these tests
during a normal build (since the tests will fail if the full deployment is not
in place).

maven -Dmaven.test.skip=false itest

--------------------------------------------------------------------------------

WARNING:  When any Sakai project adds an new artifact to any of tomcat's shared
classloaders (including both shared/lib and common/lib), you must either add the
new dependency to the test-harness' project.xml or alert the maintainer of the
test harness to add the new dependency.  If the test harness does not list this
artifact as a dependency, then the any component relying on the artifact will
not load, and any tests relying on that component (even if it isn't a test in
your project!) will fail.

The way to think about the dependencies in the test harness is this:  the test
harness dependencies play the role of tomcat's shared/lib and common/lib
directories.  If you expect a jar to be globally available to the entire system
(such as an API or a JDBC driver), you would put that jar in tomcat's
shared/lib.  If the test harness is to emulate tomcat's behavior, you should add
the artifact as a dependency to /test-harness/project.xml so it is globally
available in the test harness.

--------------------------------------------------------------------------------

INVITATION: If you've read this far, you undoubtedly recognize how fragile the
test harness is at this point.  I invite any and all interested developers to
work on this project, whether that entails changing the component manager, the
sakai plugin, or the test harness itself.

Thanks,
Josh Holtzman
jholtzman@berkeley.edu
