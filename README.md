#play-hysterix
=============

## Intro

Inspired by Hystrix, this is a library for play framework to implement scalability patterns

Netflix Hystrix is a library which implements number of distributed patterns such as graceful fallback, circuit breaker.
Unfortunately, the library binds to rx-java (Observable) and for Play framework conversion to promises or adapter to
Enumerator is a non trivial task. In addition hystrix has been developed initially with synchronous clients in mind and then
that was extended to async client, in turn this mean API is suffering from being bound to sync access in number of places.
Moreover, hystrix internally uses many ThreadLocal variables to store request state. We do not share a vision that it neither
necessary nor recommended.

## Support

Supported Play for Java 2.3.x and binary for Scala 2.10.x (should work in Scala version as well)

## Status 
status: alpha, use at own risk, a few alpha libraries pushed to maven central at:

http://repo1.maven.org/maven2/pl/matisoft/play-hysterix_2.10/

Sbt: "pl.matisoft" %% "play-hysterix" % "0.1.5.play23"

## Features:
- fallback support
- request based cache
- async access to request cache for logging request metrics

## Demo
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

## TODO
- INSPECT THREAD SAFETY
- metrics - global stats for all requests
- stream of json (as a plugabble Play Controller) to be used by Hystrix webapp (in Hystrix compatible format so we can use their dashboard application)
- circuit breaker support
- use akka to publish metrics to a central server
- graphite reporter?
