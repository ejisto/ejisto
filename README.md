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

How it works?
-------------
No magic here: ejisto makes a full scan of your web application classes during configuration process, registering all the relevant classes and their fields (no source/binary files are modified during the scan)
After that, you can start the embedded Servlet Container instance; then ejisto registers itself as instrumentation agent and dynamically modifies registered java classes at load time, giving you a fully featured application without worries about configuration, integration issues and so on.


![how it works](http://4.bp.blogspot.com/-WAjUeQOE5hA/T3OArUNqifI/AAAAAAAAADU/PM6r_YAVFAg/s1600/classloading-web.png)
