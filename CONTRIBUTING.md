You found something you did not like in our code? A bug? A great idea for a new feature? We'd love to hear from you!

# Contributing code

Please test your code before submitting a pull request. Our build is [Maven](http://maven.apache.org/) based, it should be as simple as running `mvn clean verify`. If that fails, try to fix it. If you do not know how, send a pull request anyway. We will not merge it directly, but a pull request is a great way to start a discussion about how to fix or improve the code you want to send our way.

There are a few rules in our build that will bite you at some point:

## Package Cycles

You shall not create cycles between packages. Ever. Package cycles are bad design. We have a [maven-enforcer-plugin](http://maven.apache.org/enforcer/maven-enforcer-plugin/) rule that will make the build fail if a package cycle is detected.

## Dependencies rules

We use [Macker](https://innig.net/macker/) to enforce a few "architectural" rules. Error messages should be more or less clear on the intent of those rules. If it's not the case, ask and we will make it clearer.

## Unit test coverage

Testing is important. Period. Our build enforces minimal line and branch coverage. There are exceptions in some modules to reduce this minimal coverage (look for Maven properties called `verify.*`), but that should be the exception. If you submit code, please send unit test as well. If you don't know how to test your code, we'll help you.

## Mutation coverage

We use [PIT](http://pitest.org/) for mutation testing. The basic idea is that if you test your code well, any change to your code (mutation) should make your test fail. If tests do not meet minimal mutation threshold, the build will fail.

## Coding style

Some simple rules are enforced by [Checkstyle](http://checkstyle.sourceforge.net/) at build time. In particular:

* Star imports are not allowed
* imports should organized according to the rules below, settings for [IntelliJ](https://www.jetbrains.com/idea/) are available in the `ide-settings` directory:
  * `java.*`
  * `<blank_line>`
  * `javax.*`
  * `<blank_line>`
  * `org.jmxtrans.*`
  * `<blank_line>`
  * `<other_imports.*>`
  * `<blank_line>`
  * followed by static imports, following the same order

Those 2 rules helps a lot in code review / merges, reducing the noise seen in pull requests.

## Findbugs

All code is validated by [Findbugs](http://findbugs.sourceforge.net/) standard rules at build time. Any violation will make the build fail. Findbugs error messages and documentation is not as straightforward as it should be in a few cases. We'll help you decipher the messages if you do not understand why Findbugs thinks your code is bad. And if you find a Findbugs rule with which you disagree, send a PR to disable it.

## License header

Project is MIT licensed. License header should be included in all files. This is checked by the [license-maven-plugin](http://code.mycila.com/license-maven-plugin/). To add correct headers to all your files, just use `mvn license:format`.

## Maven pom should be sorted

Maven `pom.xml` should be sorted according to Maven recommendations. This is checked by the [maven-sortpom-plugin](http://code.google.com/p/sortpom/). To sort the poms, just run `mvn sortpom:sort`. Again, this helps in code review / merges by ensuring that order of elements in pom are stable and not reorganized in arbitrary ways.

## Cleanup your commits

If your used to working with git, please rebase your branch before sending a pull request. Cleanup your history if it make sense. Squash similar commits, split different concerns. Dont spend too much time on this, we'll rewrite your branch ourself if required.
