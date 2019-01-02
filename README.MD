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

There is a also support for existing Spring Healthchecks with the ```@IndicatorCheck``` and if desired a ```TaskedHealthIndicator``` with will schedule a 
standard Spring Healthcheck to run at regular intervals without an endpoint call.

Any request to the health endpoint ```/__health``` will be cached and not hit backend services directly thus reducing any DDOS
attacks crippling services.

## How it works

When a failure is detected it is added to an internal array (for just that check) - this is visible in the check output.

That failure will stay there until it either expires or its rolled out by additional failures - the settings for the length and expiry length
are detailed below.

Once the number of failures in the array hits the alert threshold (see below) then it will set the ```ok``` status to false.

Only once the the failures drop below the threshold will it revert back to true.

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
    public LiveCheckService getLiveCheckService(LiveCheckConfiguration liveCheckConfiguration, @Autowired(required = false) List<TaskedHealthIndicator> healthChecks) {
        return new LiveCheckService(liveCheckConfiguration, healthChecks);
    }
}
```


Create a healthcheck YAML configuration file - it has the format:

```yaml
name: //Your service name
systemId: //A unique identifier of your service (no spaces)
description: //A simple description
checks:  //Array of checks
  - identifier: //A unique identifier (no spaces)  - you will use this in your annotations
    name: //The friendly name
    businessImpact: //descrive the business impact
    technicalSummary: //describe the technical summary
    severity: //HIGH, MEDIUM or LOW
    serviceTier: //BRONZE, SILVER, GOLD and PLATINUM
    panicGuide: //A URL to your help guide
    checkCount: //Default is 10 - number of checks to take into consideration when determining the failure rate.
    checkFailureThresholdPercentage: //The percentage allowed to fail before alerting. (range 0-100)
    checkExpiresSeconds: //The length of time a check will stay in consideration for before expiring 
```

Examples:

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

To setup a check and override the defaults:

```yaml
  - identifier: db-check
    name: DB Check
    businessImpact: The service will not be able to auto suggest on our website.
    technicalSummary: The DB servicing the auto-suggest functionality is not available.
    severity: LOW
    serviceTier: GOLD
    panicGuide: https://docs.mysite.com/panic/db-check
    checkCount: 5
    checkFailureThresholdPercentage: 60
    checkExpiresSeconds: 300
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


### Indicator Check

The indicator check is an annotation to wrap the ```health()``` method within a ```HealthIndicator```.

It simply inspects the result for ```Status.DOWN``` (or catches an exception) and handles it in the same way as a @LiveCheck of course the
configuration for the check has to be configured too in the YAML file.

```java
@Component
public class HealthCheck implements HealthIndicator {

    @IndicatorCheck(id = "hc", message = "Healthcheck is down")
    @Override
    public org.springframework.boot.actuate.health.Health health() {
        return Health.down().build();
    }

}
```

### TaskedHealthIndicator

If you wish to use your standard spring based healthchecks and poll them regularly rather than hitting the http endpoint then you
can implement this interface:

```java
@Component
public class HealthCheck implements HealthIndicator, TaskedHealthIndicator {

    @IndicatorCheck(id = "hc", message = "Healthcheck is down")
    @Override
    public org.springframework.boot.actuate.health.Health health() {

        return Health.down().build();
    }

    @Override
    public long period() {
        return 20000L;
    }
}
```

Of course it is not linked to the @IndicatorCheck annotation and can be excluded if desired.

This will ping the health check every 20 seconds.

