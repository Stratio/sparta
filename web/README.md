<div align="center">
<img src="https://github.com/Stratio/egeo-web/blob/master/src/assets/images/egeo_logo_c.png">
</div>

# Stratio Sparta web

## File Structure

```
web/
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

### Work with the code

You can use Npm or Yarn to work with the Sparta web. If you want to use Yarn, it has to be installed first as a global dependency in your local machine.

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

To run sparta web locally you must use this commands.

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
npm run test -- -- component-name
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