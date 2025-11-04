# pagopa eCommerce commons

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-ecommerce-commons&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-ecommerce-commons)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-ecommerce-commons&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pagopa_pagopa-ecommerce-commons)

A utility library for the [eCommerce](https://github.com/topics/pagopa-ecommerce) project.

## Requirements

- Java 21 or higher
- Maven 3.6+ for building

## CI

Repo has Github workflow and actions that trigger Azure devops deploy pipeline once a PR is merged on main branch.

In order to properly set version bump parameters for call Azure devops deploy pipelines will be check for the following
tags presence during PR analysis:

| Tag                | Semantic versioning scope | Meaning                                                           |
|--------------------|---------------------------|-------------------------------------------------------------------|
| patch              | Application version       | Patch-bump application version into pom.xml and Chart app version |
| minor              | Application version       | Minor-bump application version into pom.xml and Chart app version |
| major              | Application version       | Major-bump application version into pom.xml and Chart app version |
| skip-release       | Any                       | The release will be skipped altogether                            |

For the check to be successfully passed only one of the `Application version` labels labels must be present for a given PR or the `skip-release` for skipping release step
