<div align="center">
<img src="https://github.com/Stratio/egeo-web/blob/master/src/assets/images/egeo_logo_c.png">
</div>

# Egeo-starter

egeo-starter is an starter for Angular 2 applications that includes the [Egeo Library of components](https://github.com/Stratio/egeo) developed by [Stratio](http://www.stratio.com). This starter is ready to work with AoT, HMR, DLL, Karma, Docker and much more, setting its strengths in performance, testing and deployment.

It is even possible to connect dinamically the webapp with the server side through Docker configuration. Read about this feature and much more in [the wiki of the project](https://github.com/Stratio/egeo-starter/wiki).

But this is only a part of the Egeo project. Check the rest of our reports to know more about:

* [egeo](https://github.com/Stratio/egeo): the library of components used to build Stratio's applications.
* [egeo-web](https://github.com/Stratio/egeo-web): The official website of Egeo where documentation will be available soon.
* [egeo-ui-base](https://github.com/Stratio/egeo-ui-base): A Sass library that helps us to build our styles, including a rewritten Sass version of [flexboxgrid](http://flexboxgrid.com/).
* [egeo-theme](https://github.com/Stratio/egeo-theme): The egeo components are thematizable. This is the official theme used in the Stratio's applications.

## Table of contents

* [About this repo](#about-this-repo)
* [File Structure](#file-structure)
* [Getting Started](#getting-started)
   * [Dependencies](#dependencies)
   * [Installing](#installing)
   * [Work with the code](#work-with-the-code)
   * [How to run](#how-to-run)
   * [How to test](#how-to-test)
   * [How to build](#how-to-build)
* [Contributing](#contributing)
* [License](#license)

## About this Repo

This repo includes the whole needed to begin a new Angular 2 App, including unit testing platform with Karma, deploy environment with docker and the library of components of Egeo.

We are also using [HMR](https://github.com/AngularClass/angular2-hmr) and [DLL](https://robertknight.github.io/posts/webpack-dll-plugins/) to dramatically speed your builds.

* Documentation website (soon)

## File Structure

```
egeo-starter/
 ├──config/                        * our configuration
 |   ├──karma/                     * karma configuration
 |   └──webpack/                   * webpack configuration
 |       ├──dev-server.js          * 
 |       ├──entry.dev.js           * 
 |       ├──entry.prod.js          * 
 |       ├──output.js              * 
 |       ├──plugins.js             * 
 |       ├──resolve.js             * 
 |       ├──rules.js               * 
 |       ├──webpack.dev.js         * specifical configuration for development environment
 |       ├──webpack.prod.js        * specifical configuration for production
 |       └──webpack.test.js        * specifical configuration for execute karma
 |   ├──empty.js                   * special file needed for webpack
 |   └──helpers.js                 * utilities file for webpack
 │
 ├──docker/                        * docker configuration
 |   ├──docker_entrypoint          * the docker entrypoint
 |   └──nginx.conf                 * nginx server configuration
 │
 ├──src/                           * code of the website
 |   ├──app/                       * the angular 2 app of the website
 |   ├──assets/                    * images, fonts and other static resources
 │   ├──alias.js                   * 
 │   ├──config.json                * here you can configure an URL for connect the app with an API
 │   ├──custom-typings.d.ts        * typing definitions for typescript
 |   ├──index.html                 * website entry point
 |   ├──main.dev.ts                * Angular 2 bootstrap
 |   ├──main.ts                    * bootloader of the webapp
 |   ├──polyfills.ts               * polyfills used by the webapp
 |   └──vendor.ts                  * load of the libraries needed
 │
 ├──.htmlhintrc                    * our htmlhint linting configuration
 ├──.sass-lint.yml                 * our sass linting configuration
 ├──tslint.json                    * typescript lint config
 ├──jenkinsfile                    * configuration of our jenkins process
 ├──Dockerfile                     * configuration of our docker process
 ├──karma.conf.js                  * index file for the karma configuration
 ├──pom.xml                        * configuration to work with our CI system
 ├──tsconfig.lib.json              * settings of typescript to build the library using webpack
 ├──tsconfig.gulp.json             * settings of typescript to build the library using gulp
 ├──tsconfig.json                  * default settings of typescript
 ├──package.json                   * what npm uses to manage it's dependencies
 ├──webpack.config.js              * entrypoint to the webpack configuration
 └──yarn.lock                      * need in order to get consistent installs across machines using yarn

```

## Getting Started

### Dependencies

What you need to run this app:
* [`node`](https://nodejs.org/es/) and `npm`
* Ensure you're running the latest versions Node `v6.x.x` and NPM `3.x.x`+

What your app will need to work with Egeo:
* angular/common ~ 2.4.6",
* angular/core ~ 2.4.6",
* angular/forms ~ 2.4.6",
* angular/http ~ 2.4.6",
* angular/platform-browser-dynamic ~ 2.4.6",
* angular/Router ~ 3.4.6
* Lodash 4.17.4
* Reflect Metadata ~ 0.1.9
* Typescript 2.0.x
* Angular2 Virtual Scroll ~ 0.1.3

### Installing

You can install egeo-starter from npm:

```
npm i @stratio/egeo-starter
```

### Work with the code

You can use Npm or Yarn to work with the starter. If you want to use Yarn, it has to be installed first as a global dependency in your local machine.

```
sudo npm i -g yarn
```

Once Yarn is installed or Npm is ready, you can install dependencies using:

```
yarn
```

or

```
npm install
```

### How to Run

To run egeo-starter locally you must use this commands.

```
yarn start
```

or

```
npm run start
```

### How to Test

There is a command to start the karma server and launch the whole tests written.

```
yarn test
```

or

```
npm run test
```

It is possible to run an individual test to avoid run the whole suite.

```
npm run test --component=st-two-list
```

### How to Build

If you want to build a distributable package you must use the `build` command. This will create a **target** folder with the distributable code of the package.

```
yarn build
```

or

```
npm run build
```

## Contributing

There are many ways to contribute to the egeo-starter project. [Check our contribution section in the Wiki to learn more](https://github.com/Stratio/egeo-starter/wiki/How-to-contribute).

## License

Egeo-starter is distributed under the Apache 2 license. You may obtain a copy of the license here at:

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
