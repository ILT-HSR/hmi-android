GCS - GroundControlStation HSR
==============================

GCS is a ground control station developed at HSR for easy coordination of multiple unmanned vehicles without hours of prior training.

# Installation

Open the following links in a browser on your Android device to install the newest release:

## Stable Releases

[![Build Status](https://travis-ci.com/ILT-HSR/hmi-android.svg?branch=master)](https://travis-ci.com/ILT-HSR/hmi-android) [![Master Docs](https://img.shields.io/badge/docs-master-blue.svg)](https://ilt-hsr.github.io/hmi-android/master)

| Build Type | Link   |
|------------|--------|
| Debug      | _TBD_  |

## Development Releases

[![Build Status](https://travis-ci.com/ILT-HSR/hmi-android.svg?branch=develop)](https://travis-ci.com/ILT-HSR/hmi-android) [![Develop Docs](https://img.shields.io/badge/docs-develop-blue.svg)](https://ilt-hsr.github.io/hmi-android/develop)

| Build Type | Link   |
|------------|--------|
| Debug      | _TBD_  |

# Build

The master branch always contains a stable version of the application. The developer branch on the other side contains unstable versions.

## Howto

To build either of these branches, do the following:

1. Clone this repository (use `git clone --recursive` to include submodules).
2. Navigate to the root directory on your command line.
3. Run the command `./gradlew build`.
4. The generated apk can be found in the subdirectory `app/build/outputs/apk/debug/`.
5. Copy this apk to your Android phone to install.

Alternatively, you can use Android Studio to build and run the application.

# Contribution

Developing a ground control station requires a lot of work. If you think you have an important contribution, please do not hesitate.

## Howto

Follow the steps below to contribute to this project:

1. Clone this repository (use `git clone --recursive` to include submodules).
2. Check out the `develop` branch (if not already checked out).
3. Create a feature branch using GitFlow.
4. Make your changes.
5. Build the app and run all tests locally.
6. Create a merge request onto the `develop` branch.

## Guidelines

To contribute to this project, we require you to follow a few simple guidelines:

- **GitFlow** - All development in this repository is done following the GitFlow workflow. The `master` and `develop` branch are therefore protected and feature branches have to be used to make adjustments and contributions.
- **Commit Messages** - Outline a broad description of your change in the first line of your commit,
and keep it short.
- **Issue Tracking** - Please update/adjust issues you are working on. If your feature is not described in an issue yet, please create a new issue. This also holds if you find a bug or have a feature request without wanting to contribute.

# Documentation

Click on the `docs` badges above. This will route you to the documentation for the corresponding release.
