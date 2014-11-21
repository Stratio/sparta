# CHANGELOG

## v0.1

### Driver
- Akka actors (Supervisor and StreamingContextActor)
- Json parser
- First approach streaming context generator

### Aggregator
- Dimensions
- Rollups
- Pass through bucketer
- DateTime bucketer
- GeoHash bucketer
- Count operator
- First approach Mongo output
- KeyValue parser
- Temporal TwitterParser (demo purposes)

## v0.2

- Created new maven modules:
    - doc: Documentation module.
    - serving-core: Simple library to read aggregation data.
    - serving-api: REST api implementing serving-core.
    - plugins: Now, the platform is **pluggable**. This module contains all *official* plugins.
    - sdk: This library can be used to develop new plugins to adapt sparkta to your necessities.
- Tag bucketer
- Hierarchy bucketer
- Improvements in flume and kafka input
- Added new operator max
- Added new operator min
- Added new operator sum
- Added new datetime parser
- Added new morphlines parser
- Now a distribution is generated


