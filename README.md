About
=====

Moonshine is an application framework based on [Google Guice](https://code.google.com/p/google-guice/) dependency injection library.

It is based on the notion of 'Service'. Services implement specific functionality and they register bindings
in Guice to export this functionality to other services.

Moonshine provides mechanisms for configuring the services, prepares the environment for executing them
and gives you the utilities to easy test them.

Out of the box Moonshine provides ready to use services for database support (H2, HSQLDB), JTA (Atomikos), servlets (Jetty),
JPA (Hibernate), JMX, JAX-RS (Jersey), and many more.

Changes
=======

1.0 Initial Moonshine revision, beta quality

License
=======

Moonshine is available under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Documentation
=============

[Manual](http://moonshine.atteo.org/manual/)

[Javadoc](http://moonshine.atteo.org/apidocs/)

Tutorial: [How to create blog application with Moonshine](http://moonshine.atteo.org/tutorial/)
