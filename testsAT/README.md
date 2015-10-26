# README

## ACCEPTANCE TESTS

Cucumber automated and manual acceptance tests.
This module depends on a QA library (stratio-test-bdd), where common logic and steps are implemented.

## EXECUTION

These tests will be executed as part of the continuous integration flow as follows:

mvn verify [-D<ENV_VAR>=<VALUE>] [-Dit.test=<TEST_TO_EXECUTE>|-Dgroups=<GROUP_TO_EXECUTE>]

By default, in jenkins we will execute the group basic, which should contain a subset of tests, that are to key to the functioning of the module and the ones generated for the new feature.

All tests, that are not fully implemented, should belong to the group manual and be tagged with '@ignore @unimplemented'
