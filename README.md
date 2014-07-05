#play-hysterix

## Intro

Inspired by Hystrix, this is a library for play framework to implement scalability patterns.

Netflix Hystrix is a library which implements number of distributed patterns such as graceful fallback, circuit breaker.
Unfortunately, the library binds to rx-java (Observable) and for Play framework conversion to promises or adapter to
Enumerator is a non trivial task. In addition hystrix has been developed initially with synchronous clients in mind and then
that was extended to async client, in turn this mean API is suffering from being bound to sync access in number of places.
Moreover, hystrix internally uses many ThreadLocal variables to store request state. We do not share a vision that
using thread locals neither necessary nor recommended. In addition it was for author of this library a big surprise how HttpRequestLog is initialized in hysterix,
basically that without invoking shutdown method after initialize memory leaks could occur. Instead hysterix is a bit more verbose, one
has to create manually or via AOP or filter HysterixContext, which will be passed to library and eventually garbage collected. 

In addition, contrary to Hystrix, this library does not use request collapers from hystrix (neither batch size nor time collapsing). This concept we believe
is ultimately broken and anything based on time or size may leak through cache. Hysterix uses lazy scala promises and only invokes promise onCompleted
or onFailure in case real response has been returned from the server.

However, this library does not want to reinvent the wheel and initial plan is to be compatible with hystrix-dashboard json api, so that results can be displayed using Netlix UI tools. 

## Support

- Support for Play for Java 2.3.x and binary for Scala 2.10.x (should work in Play Scala version as well). 
Scala 2.11.x binary on request.
- Java 8 is required at the moment

## Status 
status: in development, use at own risk (interface may change and may be buggy), a few initial versions pushed to maven central at:

http://repo1.maven.org/maven2/pl/matisoft/play-hysterix_2.10/

Sbt: "pl.matisoft" %% "play-hysterix" % "0.2.6"

## Features:
- graceful handling support for commands, also as a Promise (i.e. remote service call or plain value)
- request based cache (without any request collapsers but using promises)
- async access to request cache for logging request metrics (timeout based)
- support for global metrics for all commands (HysterixGlobalStatistics) and streaming some data to hysterix-dashboard (HysterixController)
- safe - no memory leaks possibility by design, hysterix request context should be garbage collected after each http request
- time windowed and global statistics for requests (master)

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
- 0.2.x - (Play 2.3.x only) changed remote calls from warn to error and improved concurrency

## TODO
- graphite reporter
- semaphore - to limit number of concurrent requests from a server to prevent (network and io contention)
- configurable retry counter and retry delay - does not help to fail fast but somebody may like this
- more intelligent circuit breaker, maybe slowly reducing load as opposed to binary flip, e.g. sentries project (https://github.com/erikvanoosten/sentries)
- rewrite to Scala and use Scala future, enable Java API to work
- think over how to detect an end to web request -> in HysterixRequestLog (is really tricky, since we don't know number of requests (commands) upfront)
- JavaDocs
- Unit tests
