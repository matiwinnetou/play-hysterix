#play-hysterix
=============

Supported Play for Java 2.3.x (should work in Scala version as well)

Inspired by Hystrix, this is a library for play framework to implement scalability patterns

status: alpha, use at own risk

## Features:
- fallback support
- request based cache
- sync access to request cache (should be async)

## TODO
- INSPECT THREAD SAFETY
- request log - asynchronous API
- metrics - global stats for all requests
- stream of json to be used by Hystrix webapp (in compatible format)
- circuit breaker support
