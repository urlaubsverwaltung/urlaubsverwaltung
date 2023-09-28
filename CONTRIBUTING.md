# Contributing to Urlaubsverwaltung

The Urlaubsverwaltung is released under the Apache 2.0 license. If you would like to contribute
something, or simply want to hack on the code this document should help you get started.

## Ideal for contribution

We would be more than pleased if the community would help us build a awesome piece of software for all. To find issues and pull requests that are good for contributions you can use the ['ideal for contribution'](https://github.com/urlaubsverwaltung/urlaubsverwaltung/issues?q=is%3Aissue+is%3Aopen+label%3A%22status%3A+ideal-for-contribution%22) label.

## Using GitHub Issues

We use GitHub issues to track bugs and enhancements.

If you are reporting a bug, please help to speed up problem diagnosis by providing as much
information as possible.

Please look for other issues or pull requests which already work on this topic. Is somebody already on it? Do you need to synchronize?

Have you already checked the [FAQ](https://github.com/urlaubsverwaltung/urlaubsverwaltung/wiki)?


## Reporting Security Vulnerabilities

If you think you have found a security vulnerability in the Urlaubsverwaltung please *DO NOT*
disclose it publicly until we've had a chance to fix it. Please *don't report security
vulnerabilities using GitHub issues*, instead open a report of a security vulnerability
via [Report a security vulnerability](https://github.com/urlaubsverwaltung/urlaubsverwaltung/security/advisories/new)


## Pull Request Contribution

New pull requests will be labeled with status of process as shown below:

![Pull request workflow](.docs/workflow-pullrequests.svg)


## Code Conventions and Housekeeping
None of these is essential for a pull request, but they will all help.
They can also be added after the original pull request but before a merge.

* We use [EditorConfig](https://editorconfig.org/) project to apply code formatting conventions.
* Add some Javadocs.
* A few unit tests would help a lot as well -- someone has to do it.
* If no-one else is using your branch, please rebase it against the current main (or
  other target branch in the main project).
* When writing a commit message please follow [these conventions](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html),
  if you are fixing an existing issue please add `Fixes gh-XXXX` at the end of the commit
  message (where `XXXX` is the issue number).


## Working with the Code
If you don't have an IDE preference we would recommend that you use
[the Intellij Community Edition](https://www.jetbrains.com/idea/download). This will
automatically use our EditorConfig.


### Building from Source
The Urlaubsverwaltungs source can be build from the command line using
[Apache Maven](http://maven.apache.org/run-maven/index.html) on JDK 21 or above.
We include '`Maven Wrapper`' scripts (`./mvnw` or `mvnw.bat`) that you can run rather
than needing to install Maven locally.


#### Default Build
The project can be built from the root directory using the standard Maven command:

	$ ./mvnw clean verify
