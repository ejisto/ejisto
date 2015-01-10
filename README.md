ejisto
======

[![Build Status](https://travis-ci.org/ejisto/ejisto.png?branch=master)](https://travis-ci.org/ejisto/ejisto) [ ![Download](https://api.bintray.com/packages/cbellone/generic/ejisto/images/download.png) ](https://bintray.com/cbellone/generic/ejisto/_latestVersion)

What is ejisto?
---------------

Developing the "presentation" layer of a distributed web application is a hard life.
You have to fight every day against database synchronization, binary data incompatibility, system integration failures and so on.

But do you really care...

* if database is up to date?
* if two systems are well interconnected?
* if remote server is up and running?
* if third-party web service is doing its job?

These things are not actually related with your job.
You should focus on the front-end part of the application, but you should be also able to see (and test) your changes to a dynamic page.

How to do that?
---------------

There are two possible ways:

* ask your colleagues to write tons of mock objects for each kind of connector, and keep them always up-to-date. For each release, for each little data model modification or remote interface change
* use ejisto!!

ejisto is (or will be) a sandbox for your application. It moves the focus on the front-end side cutting all the links between your pages and the rest of the world.

How does it work?
-------------
No magic here: ejisto makes a full scan of your web application classes during configuration process, registering all the relevant classes and their fields (no source/binary files are modified during the scan)
After that, you can start the embedded Servlet Container instance; then ejisto registers itself as instrumentation agent and dynamically modifies registered java classes at load time, giving you a fully featured application without worries about configuration, integration issues and so on.


![how does it work](http://4.bp.blogspot.com/-WAjUeQOE5hA/T3OArUNqifI/AAAAAAAAADU/PM6r_YAVFAg/s1600/classloading-web.png)

May I try it?
-----------------
Of course! You can either download the latest release or the source code from github.

### Prerequisites
The Java runtime environment (JRE) **version 8** or later is required to start the application. You can download it from [java.com](http://java.com).
To check the version of your JRE, you can use the following command on your favourite shell:

```
$ java -version
```

Ejisto should run on all systems already supported by the JRE. However, at the moment it is developed and tested only on [Slackware Linux](http://www.slackware.com). 
Should you have problems on other systems, please create an issue describing what actually happened (an attached log file would be really helpful)

### Download and launch the latest release
The latest release can be found on the [bintray](https://bintray.com/cbellone/generic/ejisto/_latestVersion) repository.
After you downloaded and unzipped the application archive, launch the following command, depending on your platform:

linux / mac (?):

```
$ /your-installation-path/ejisto/ejisto.sh
```

windows:

```
> C:\your-installation-path\ejisto\ejisto.bat
```

### Build from sources
Please note that a JDK 8+ and [Apache Maven](http://maven.apache.org) are required in order to build and run the application.
After cloning ejisto's repository, try to build the application:

```
$ mvn -am -pl application clean install
```


launch the application:

```
$ cd application
$ mvn exec:exec
```

and then open the following URL with your favourite browser

[http://localhost:6789](http://localhost:6789)

____

Of course, all registered names are Copyright of their respective owners. 
