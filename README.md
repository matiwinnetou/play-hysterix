#play-hysterix

## Intro

Inspired by Hystrix, this is a library for play framework to implement scalability patterns.

Netflix Hystrix is a library which implements number of distributed patterns such as graceful fallback, circuit breaker.

Number of notable differences comparing to Netflix Hystrix:
- library does not bind to RxJava, which may not be desired for play users, that could still be provided by a contrib module
- hystrix has been developed initially with synchronous clients and despite the fact that there is a HystrixObservableCommand, others parts of library assume sync access
- hystrix internally uses many ThreadLocal variables to store request state, alternatively hysterix passes HysterixRequestContext, which is more verbose but more secure in terms of memory cleanup (GC will automatically collect context object)
- hysterix does not user a concept from hystrix - request collapsers (neither batch nor time collapsing), this concept is not ideal because anything based on time or size may leak through cache, it is hard to predict how fast number of requests will be done, hysterix uses redeemable promises as a way to realize request based cache 
- library is compatible with Hystrix dashboard, which is a nice UI to visualize hystrix metrics

## Requirements

- Play for Java 2.3.x and binary available for Scala 2.10.x (cross compilation for Scala 2.11.x binary on request).
- Java 8

## Status 
status: used in production, interface stable, only additions planned

http://repo1.maven.org/maven2/pl/matisoft/play-hysterix_2.10/

Sbt: "pl.matisoft" %% "play-hysterix" % "0.2.10"

## Features:
- graceful handling support for commands, also as a Promise (i.e. remote service call or plain value)
- request based cache (without any request collapsers but using promises)
- async access to request cache for logging request metrics (timeout based)
- support for global metrics for all commands (HysterixGlobalStatistics) and streaming some data to hysterix-dashboard (HysterixController)
- safe - no memory leaks possibility by design, hysterix request context should be garbage collected after each http request
- time windowed and global statistics for requests 
- circuit breaker support

## Authors:
- Mateusz Szczap
- Sergiusz Urbaniak

## Demo of request log output (could be shown in a debug mode at the end of page or in logs)
Hysterix Log:
OpenCms.OpenCms-modellverzeichnis - 8 ms - [SUCCESS] - http://localhost:9000/opencms/mock/modellverzeichnis/audi/a4.html
OpenCms.OpenCms-modellverzeichnis - 0 ms - [SUCCESS, RESPONSE_FROM_CACHE] - http://localhost:9000/opencms/mock/modellverzeichnis/audi/a4.html
OpenCms.OpenCms-modellverzeichnis - 0 ms - [SUCCESS, RESPONSE_FROM_CACHE] - http://localhost:9000/opencms/mock/modellverzeichnis/audi/a4.html
OpenCms.OpenCms-modellverzeichnis - 0 ms - [SUCCESS, RESPONSE_FROM_CACHE] - http://localhost:9000/opencms/mock/modellverzeichnis/audi/a4.html
OpenCms.OpenCms-modellverzeichnis - 0 ms - [SUCCESS, RESPONSE_FROM_CACHE] - http://localhost:9000/opencms/mock/modellverzeichnis/audi/a4.html
SeoImageGalleryService.ImageGalleryOverviewCommand - 10 ms - [SUCCESS] - http://localhost:9004/seo-gallery-service/overview/by-id/1900/9?page-size=2147483647&order-by=NATURAL
SeoImageGalleryService.ImageGalleryOverviewCommand - 11 ms - [SUCCESS] - http://localhost:9004/seo-gallery-service/overview/by-id/1900/9?page-size=1&order-by=NATURAL
SiteNavService.LangsCommand - 16 ms - [SUCCESS] - http://localhost:9003/site-nav-service/langs-menu
SiteNavService.MainMenu - 16 ms - [SUCCESS] - http://localhost:9003/site-nav-service/main-menu
MOBILE_SVC_API.FetchModelsCommand - 99 ms - [SUCCESS] - http://m.mobile.de/svc/r/models/1900
MOBILE_SVC_API.FetchMakesCommand - 101 ms - [SUCCESS] - http://m.mobile.de/svc/r/makes/Car
MOBILE_SVC_API.FetchMakesCommand - 100 ms - [SUCCESS, RESPONSE_FROM_CACHE] - http://m.mobile.de/svc/r/makes/Car

## Version history
- 0.1.11 - bug fix for average execution time calculation
- 0.1.12 - bug fix in case request caching was disabled, markSuccess was never invoked.
- 0.2.0 - initial support for streaming hysterix stream to hystrix dashboard UI
- 0.2.1 - improved generics handling, HysterixRequestsCache is now generics enabled + percentiles and bug fixes
- 0.2.2 - concurrency bug fix in controller
- 0.2.3 - initial support for circuit breaker + bug fixes
- 0.2.4 - replaced synchronized with ReentrantLock and simplified request cache
- 0.2.5 - time windowed and global statistics for requests
- 0.2.6 - (Play 2.3.x only) typo fix in a method name
- 0.2.7 - (Play 2.3.x only) changed remote calls from warn to error and improved concurrency handling
- 0.2.8 - (Play 2.3.x only) fixed a small bug in HysterixCommand, replaced new Timer, which created a new thread on each request with ScheduledExecutorService, moved to sbt release plugin, introduced runtime HysterixException
- 0.2.9 - (Play 2.3.x only) upgrade to latest yammer metrics-core library
- 0.2.10 - (Play 2.3.x only) major PROD bug fix causing a thread leak in case RequestInspectLog was enabled (default setting)

## TODO
- graphite reporter
- semaphore - to limit number of concurrent requests from a server to prevent (network and io contention)
- configurable retry counter and retry delay - does not help to fail fast but somebody may like this
- more intelligent circuit breaker, maybe slowly reducing load as opposed to binary flip, e.g. sentries project (https://github.com/erikvanoosten/sentries)
- rewrite to Scala and use Scala future, enable Java API to work
- think over how to detect an end to web request -> in HysterixRequestLog (is really tricky, since we don't know number of requests (commands) upfront)
- JavaDocs
- Unit tests
