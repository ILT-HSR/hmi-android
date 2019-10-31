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

# Running in a PX4 Simulation Environment

GCS supports interaction with PX4 SITL/HITL simulation environments. In order to run in such an
environment, you will need to follow the steps outlined below.

## Prepare Custom AVD Emulation

Since the introduction of the WiFi feature in the Android emulator, there is an
[issue](https://issuetracker.google.com/issues/37095198) that prevents UDP redirection from working
if this feature is enabled. Therefore, you will need to start the emulator manually instead of
launching it directly from Android Studio. Make sure to disable WiFi using a command like this:

```bash
$ emulator -avd <YOUR_AVD_ID> -feature -Wifi
```

## Enable UDP Forwarding in the Emulator

The PX4 simulation uses UDP to communicate with base stations. It expects the GCS to listen on port
`14550`. Since the emulator's network is NATed through the host, you need to enable UDP redirection
using the emulator's Telnet interface:

```bash
$ telnet 127.0.0.1 5554
> auth <YOUR_EMULATOR_AUTH_KEY>
> redir add udp:14550:14550
```

## Running the PX4 Simulation

First make sure you have a copy of the [PX4 firmware](https://github.com/PX4/Firmware) repository
available. Enter into the root of your copy and run the following command:

```bash
$ make px4_sitl_default jmavsim
```

This will start the PX4 simulation using jMAVSim. You might want to adjust your simulated GPS
location to something near where your Android emulator believes it is located. You can do that
by setting the `PX4_HOME_LAT`, `PX4_HOME_LON`, and `PX4_HOME_ALT` environment variables before
starting the simulation. For example, in order to set the location to the main building of HSR, you
can use the following command:

```
$ PX4_HOME_LAT=47.2233607 PX4_HOME_LON=8.8173627 make px4_sitl_default jmavsim
```

## Start the GCS Application in the Emulator

You are now equipped to start using the simulation environment from the GCS application. Make sure
to install the application in the emulator (for example by running it from within Android Studio).
Before you can use the application in conjunction with the simulation, you have to make sure that
the application uses the UDP data channel for vehicle communication. You can select the channel in
the GCS preferences activity.

# Contributing

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
