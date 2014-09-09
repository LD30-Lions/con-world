## Introduction

A play-clj game in which you play an underground organism that feeds a plant.

## Contents

* `android/src` Android-specific code
* `desktop/resources` Images, audio, and other files
* `desktop/src` Desktop-specific code
* `desktop/src-common` Cross-platform game code
* `ios/src` iOS-specific code

## Quick Build & Run

Get [Leiningen](https://github.com/technomancy/leiningen).

`git clone git@github.com:LD30-Lions/con-world.git`

`cd con-world/desktop`

`lein uberjar`

`java -jar target/con-world-0.0.1-SNAPSHOT-standalone.jar`
