This plugin aims to make developing functional tests for Grails more convenient by facilitating running your tests against an already running application. It utilises the improved functional testing support added in Grails 1.3.5 and does not work with earlier versions.

## Installation/Configuration

All you have to do is install the plugin

    grails install-plugin functional-test-development
    
## How Does It Work?

The plugin installs a script, `develop-functional-tests`, that you can use to develop your functional tests more conveniently. It manages the process of starting your application in a test environment, then repeatedly prompting you for which tests to run against it. This means that you can make changes to your tests, and re-run them without having to wait for your application to start each time.

This is probably best illustrated by example:
    
    $ grails develop-functional-tests
    Welcome to Grails 1.3.5 - http://grails.org/
    Licensed under Apache Standard License 2.0
    Grails home is set to: /usr/local/grails/active

    Base Directory: /Users/ld/Projects/myapp
    Resolving dependencies...
    Dependencies resolved in 999ms.
    Running script plugins/functional-test-development-0.1/scripts/DevelopFunctionalTests.groovy
    Environment set to development

    Launching application with 'test run-app' ...
      > Welcome to Grails 1.3.5 - http://grails.org/
      > Licensed under Apache Standard License 2.0
      > Grails home is set to: /usr/local/grails/active
      > 
      > Base Directory: /Users/ld/Projects/myapp
      > Resolving dependencies...
      > Dependencies resolved in 987ms.
      > Running script /usr/local/grails/active/scripts/RunApp.groovy
      > Environment set to test
      >    [delete] Deleting directory /Users/ld/.grails/1.3.5/projects/myapp/tomcat
      > Running Grails application..
      > Server running. Browse to http://localhost:8080/myapp

    -- Ready to run tests --
       * Enter a test target pattern to run tests (e.g. functional: Login)
       * Enter a blank line to run all functional tests
       * Enter 'restart' to restart the running application
       * Enter 'exit' to stop

    Command: _

At this point, my application is running in a _seperate process_. I am in the middle of developing some tests for logging in that are called 'LoginTests'. To run them, I enter a test targetting command like I would do with `grails test-app`…

    Command: functional: Login

    Launching tests with 'test-app -baseUrl=http://localhost:8080/myapp functional: Login' ...
      > Welcome to Grails 1.3.5 - http://grails.org/
      > Licensed under Apache Standard License 2.0
      > Grails home is set to: /usr/local/grails/active
      > 
      > Base Directory: /Users/ld/Projects/myapp
      > Resolving dependencies...
      > Dependencies resolved in 889ms.
      > Running script /usr/local/grails/active/scripts/TestApp.groovy
      > Environment set to test
      >     [mkdir] Created dir: /Users/ld/Projects/myapp/target/test-reports/html
      >     [mkdir] Created dir: /Users/ld/Projects/myapp/target/test-reports/plain
      > 
      > Starting functional test phase ...
      >   [groovyc] Compiling 1 source file to /Users/ld/Projects/myapp/target/test-classes/functional
      > 
      > -------------------------------------------------------
      > Running 1 functional test...
      > Running test LoginTests...
      >                     testLogin...FAILED
      > Tests Completed in 380ms ...
      > -------------------------------------------------------
      > Tests passed: 0
      > Tests failed: 1
      > -------------------------------------------------------
      > [junitreport] Processing /Users/ld/Projects/myapp/target/test-reports/TESTS-TestSuites.xml to /var/folders/25/25m7USH7FzCymqJLsj2piU+++TI/-Tmp-/null1776675137
      > [junitreport] Loading stylesheet /usr/local/grails/active/lib/junit-frames.xsl
      > [junitreport] Transform time: 490ms
      > [junitreport] Deleting: /var/folders/25/25m7USH7FzCymqJLsj2piU+++TI/-Tmp-/null1776675137
      > 
      > Tests FAILED - view reports in target/test-reports

    -- Ready to run tests --
       * Enter a test target pattern to run tests (e.g. functional: Login)
       * Enter a blank line to rerun the previous tests (pattern: 'functional: Login')
       * Enter 'restart' to restart the running application
       * Enter 'exit' to stop

    Command: _
    
This causes a `test-app` command to be launched in a separate process, but pointing to the already running application via the `baseUrl` argument. As of Grails 1.3.5, this instructs Grails to run the functional tests without starting up the application. This means that your tests start running quickly.

I have a failing test here. I can now open the test in my IDE or editor and fix the failing test case. I now re-run the test I ran previously by simply hitting «enter» key back in the console.

    Command: «enter»

    Launching tests with 'test-app -baseUrl=http://localhost:8080/myapp functional: Login' ...
      > Welcome to Grails 1.3.5 - http://grails.org/
      > Licensed under Apache Standard License 2.0
      > Grails home is set to: /usr/local/grails/active
      > 
      > Base Directory: /Users/ld/Projects/myapp
      > Resolving dependencies...
      > Dependencies resolved in 1033ms.
      > Running script /usr/local/grails/active/scripts/TestApp.groovy
      > Environment set to test
      >     [mkdir] Created dir: /Users/ld/Projects/myapp/target/test-reports/html
      >     [mkdir] Created dir: /Users/ld/Projects/myapp/target/test-reports/plain
      > 
      > Starting functional test phase ...
      >   [groovyc] Compiling 1 source file to /Users/ld/Projects/myapp/target/test-classes/functional
      > 
      > -------------------------------------------------------
      > Running 1 functional test...
      > Running test LoginTests...PASSED
      > Tests Completed in 424ms ...
      > -------------------------------------------------------
      > Tests passed: 1
      > Tests failed: 0
      > -------------------------------------------------------
      > [junitreport] Processing /Users/ld/Projects/myapp/target/test-reports/TESTS-TestSuites.xml to /var/folders/25/25m7USH7FzCymqJLsj2piU+++TI/-Tmp-/null1747694731
      > [junitreport] Loading stylesheet /usr/local/grails/active/lib/junit-frames.xsl
      > [junitreport] Transform time: 480ms
      > [junitreport] Deleting: /var/folders/25/25m7USH7FzCymqJLsj2piU+++TI/-Tmp-/null1747694731
      > 
      > Tests PASSED - view reports in target/test-reports

    -- Ready to run tests --
       * Enter a test target pattern to run tests (e.g. functional: Login)
       * Enter a blank line to rerun the previous tests (pattern: 'functional: Login')
       * Enter 'restart' to restart the running application
       * Enter 'exit' to stop

    Command: _

So we were able to develop/fix the test without having to incur the cost of the Grails app starting. This **will** save you time.

At this point we can enter another targetting pattern to run some different tests if need be.

## Restarting the app

If you make changes to the application, they won't be reflected without a restart. This is because the test environment does not implement reloading like the development environment.

To restart the application, you simply enter `restart` into the command prompt.

    Command: restart

    restarting app ...
    Launching application with 'test run-app' ...
      > Welcome to Grails 1.3.5 - http://grails.org/
      > Licensed under Apache Standard License 2.0
      > Grails home is set to: /usr/local/grails/active
      > 
      > Base Directory: /Users/ld/Projects/myapp
      > Resolving dependencies...
      > Dependencies resolved in 967ms.
      > Running script /usr/local/grails/active/scripts/RunApp.groovy
      > Environment set to test
      >    [delete] Deleting directory /Users/ld/.grails/1.3.5/projects/myapp/tomcat
      > Running Grails application..
      > Server running. Browse to http://localhost:8080/myapp

    -- Ready to run tests --
       * Enter a test target pattern to run tests (e.g. functional: Login)
       * Enter a blank line to rerun the previous tests (pattern: 'functional: Login')
       * Enter 'restart' to restart the running application
       * Enter 'exit' to stop

    Command: 

## About Your Tests

When your tests are run against a “remote” application in this fashion, you cannot use dependency injection or GORM (to create/modify domain data). A compelling solution to this problem is to use the [remote-control](http://github.com/alkemist/grails-remote-control) plugin that allows you to run commands inside remote applications. Using this approach, you can still easily create domain data or fiddle with beans in the application you are testing.

The remote-control plugin also works when you are running your tests 'inline' (i.e. in the same JVM as the application under test).

## Testing against a WAR or with HTTPS

Any arguments passed to the `develop-functional-tests` script are passed to the `run-app` invocation used to start your application. So if you want to test against a built WAR, you can do:

    grails develop-functional-tests -war

For https support, you can do:

    grails develop-functional-tests -https

If you restart the app (by entering the `restart` command), the same args are used. To change the args you need to exit the script and invoke it again.
