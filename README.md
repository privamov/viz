# Priva'Mov
This repository contains all code related to the [Priva'Mov project](https://liris.cnrs.fr/privamov).
For now, it mainly consists in SpotME, which is a visualization tool for mobility traces.

## Install requirements
To compile SpotME, you need to following dependencies:

  * Linux or Mac OS.
  * Python 2.x (**not** Python 3) and its development headers, for Pants.
  * A C compiler and system headers, for Pants.
  * [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), for Pants and SpotME backend.
  * [npm](https://docs.npmjs.com/getting-started/installing-node) and [Yarn](https://yarnpkg.com/en/docs/install), for SpotME frontend.
  * An Internet connection, used to bootstrap Pants and download Maven dependencies.

[Pants](http://www.pantsbuild.org/) is used to build the various components.
It allows to work with small modules and an heteregenous projects with various different languages.
Pants is installed on a per-repository basis and launched using the `pants` wrapper at the root of the repository.
If all of its requirements are met, Pants can be can bootstrapped and validated with the following command (to be launched at the root of this repository):

```bash
./pants -V
```
## Compiling
SpotME is made of two parts: a backend, which exposes a REST API, and a frontend, which provides a Web interface.
Both have two be compiled separately, starting with the frontend (which is then embedded in the backend server).

```bash
pushd src/node/fr/cnrs/liris/privamov/spotme
yarn install
npm run build
popd
./pants binary src/jvm/fr/cnrs/liris/privamov/spotme/:bin
```

Alternatively, it is possible to use the Makefile:

```bash
make spotme
```

If everything went well, a `privamov-spotme.jar` JAR file should have been created in the `dist/` directory.

## Running
SpotME can be executed like in any environment where a Java JRE 8 is installed.

```bash
java -jar dist/privamov-spotme.jar
```

By default it will work, but will not be able to display any trace.
For that, stores have to be configured with the `-viz.stores` flags.
Its value is a comma-separated list of stores to use, each store being specified under the form `name:type`, where:

  * name is an alpha-numeric identifier that will be used to refer to this store later. It must be unique across all data stores.
  * type is one of the supported data store: *privamov*, *postgres* or *filesystem*.
  
Each store is then configured with environment variables, depending on its type.
  
### Privamov data store
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_HOST`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_BASE`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_USER`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_PASS`

### Postgres data store
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_HOST`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_BASE`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_USER`
  * `QUERULOUS__${UPPER_CASE_STORE_NAME}_PASS`
 
### Filesystem data store
   * `FILESYSTEM__${UPPER_CASE_STORE_NAME}_ROOT`

## Command-line parameters reference

Here is the list of all command-line parameters supported by SpotME.
  * ` -viz.stores`: List of configured stores.
  * `-viz.firewall`: Firewall to use, either `none` or `fleet`. If the Fleet firewall is used, the lending token will be converted into a valid source, restricted in time to only the lending duration, by using the [Fleet API](https://github.com/privamov/fleet).
  * `-viz.fleet_server`: Address of the Fleet server to use, by default the live one, hosted at LIRIS.
  * `-viz.standard_limit`: Maximum number of elements that can be retrieved in standard listings.
  * `-viz.extended_limit`: Maximum number of elements that can be retrieved in extended listings (i.e., when listing features).
  * `-ui`: Whether to enable the built-in visualization tool.
