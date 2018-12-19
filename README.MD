# Spring Live Check

A common problem with some production systems that integrate with legacy systems is that its not easy to create a desired
health check by sending requests to the backend with test data to verify connectivity or validation.

The idea of this library is to wrap methods with the ``@LiveCheck`` annotation to catch any exceptions thrown from the method and 
report on it (within threshold limits).

The key benefit being that we don't need to send test data as we are using our real customer requests to monitor the system, but herein lies
its limitation - without requests we cannot monitor as expected so this process will only work for systems that usually have a 
guarantee of more than 0 requests per period.

Currently this is in beta testing on some production systems to see if it works as expected.  There are many enhancements that
can be done but only if value is identified.

There is a also support for a ```TaskedHealthIndicator``` with will schedule a standard Spring Healthcheck to run at regular 
occasions without an endpoint call.

Any request to the health endpoint ```/__health``` will be cached and not hit backend services directly thus reducing any DDOS
attacks crippling services.

## Usage

Ensure you component scan the live check packages:

```@ComponentScan(basePackages = {"com.your.package", "com.monkat.health"})```

Create a new Spring configuration

```java
@Configuration
public class HealthCheckConfiguration {

    @Bean
    public LiveCheckConfiguration getLiveCheckConfiguration() {
        return new LiveCheckStartup()
                .setupLiveCheck(LiveCheckConfiguration.class.getResourceAsStream("/health.yml"));
    }

    @Bean
    public LiveCheckService getLiveCheckService(LiveCheckConfiguration liveCheckConfiguration) {
        return new LiveCheckService(liveCheckConfiguration, new ArrayList<>());
    }
}

```

Create a healthcheck YAML configuration file:

```yaml
name: My Service Name
systemId: my-service-name
description: Handles all web request
checks:
  - identifier: rest-check
    name: API Rest Check
    businessImpact: The service cannot serve our customers.
    technicalSummary: The Rest API backend service is not responding correctly.
    severity: HIGH
    serviceTier: PLATINUM
    panicGuide: https://docs.mysite.com/panic/rest-check

  - identifier: db-check
    name: DB Check
    businessImpact: The service will not be able to auto suggest on our website.
    technicalSummary: The DB servicing the auto-suggest functionality is not available.
    severity: LOW
    serviceTier: GOLD
    panicGuide: https://docs.mysite.com/panic/db-check
```


Simply add annotations around your methods.  The ```id``` should match those configured in your configuration file and 
you may assign multiple checks tot he same ID.  The ```message``` is shown in the healthcheck output and should not contain 
anything sensitive.

```java
    @LiveCheck(id = "db-check", message = "The database get request is failing")
    public Results getData() {
        ...
    }
    
    @LiveCheck(id = "db-check", message = "The database update request is failing")
    public void updateData(Data data) {
        ...
    }
```

```java
    @LiveCheck(id = "rest-check", message = "The REST API is failing")
    public Results callAPI() throws exception {
        ...
    }
```

Note: It's important that the method should ONLY throw an exception in error circumstances.   Not for example if its calling
a backend API and receives a 404 - this may not necessarily be an unhealthy situation.


### Tasked Health Indicator

TDB