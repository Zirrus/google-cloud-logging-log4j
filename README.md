# Google Cloud Logging lo4j2 appender

This project helps logging all log messages to Google Cloud Logging via an log4j2 Appender.

## Installation

Add the following maven dependency to your `pom.xml`:

```
    <dependency>
      <groupId>eu.zirrus.gcloud.logging</groupId>
      <artifactId>log4j</artifactId>
      <version>0.1.0</version>
    </dependency>
```

or with gradle:

```
compile 'eu.zirrus.gcloud.logging:log4j:0.1.0'
```

## Configuration 

This is a sample config which adds a CloudLogging Appender called CloudLogginTest. The Appender is wrapped in an AsyncAppender to run in non-blocking mode. 

```
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="INFO">
  <Appenders>
    <CloudLogging name="CloudLoggingTest" projectId="allcyte-akira">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
    </CloudLogging>
    <Console name="Console">
      <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
    </Console>
    <Async name="CloudLoggingAsync">
      <AppenderRef ref="CloudLoggingTest"/>
    </Async>
  </Appenders>
  <Loggers>
    <Root level="info">
      <AppenderRef ref="Console" level="error"/>
      <AppenderRef ref="CloudLoggingAsync" />
    </Root>
  </Loggers>
</Configuration>
```

