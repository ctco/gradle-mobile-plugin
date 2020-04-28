# C.T.Co Mobile Plugin for Gradle

## About
The C.T.Co mobile plugin for Gradle helps you to configure and build Xcode and Xamarin (iOS, Android) apps.
You can configure a project to build multiple artifacts with profiling for specific environments and build them all with a single or only the ones you specify. You can profile plist file values, regex replace values in any text based file and replace binary files. Plugin also supports uploading artifacts to a Knappsack server.

## Requirements to build plugin
- Java 1.8
- Gradle 5.6.4

## Requirements to use plugin
- Java 1.8 or greater
- Gradle 2.14.1 or greater
- Xcode and Xcode command line tools
- Xamarin Studio, Mono framework, Xamarin.iOS, Xamarin.Android

## License
Copyright 2013 C.T.Co Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

## Usage examples
To apply plugin to your project add the following configuration to project's build.gradle:

    buildscript {
      repositories {
         mavenCentral()
      }
      dependencies {
        classpath group: 'lv.ctco.scm', name: 'gradle-mobile-plugin', version: '0.14.+'
      }
    }
    apply plugin: 'ctco-mobile'

To build an Xcode project let the plugin auto-detect environments or add a specific xcode extension configuration to project's build.gradle similar as in example below:

    ctcoMobile {
      platform = 'xcode'

      xcode {
        // General configuration
        automaticConfiguration = false
        projectName = 'APP'

        // A list of environments mapped to targets
        environment name:'DEV', target:'APP DEV'
        environment name:'UAT', target:'APP UAT'

        // A list of profiling actions for environments
        profile environment:'UAT', target:'Info.plist', source:'Profiles/Info-UAT.plist'
      }
    }

To build a Xamarin project add a specific xamarin (and xandroid) extension configuration to project's build.gradle similar as in example below:

    ctcoMobile {
      platform = 'xamarin'

      xamarin {
        // General configuration
        automaticConfiguration = false
        solutionFile = file('App.sln')
        projectFile = file('App.iOS/App.iOS.csproj')
        projectName = 'App.iOS'

        // A list of environments mapped to targets
        environment name:'DEV', configuration:'Release', platform:'iPhone'
        environment name:'UAT', configuration:'Release', platform:'iPhone'

        // A list of profiling actions for environments
        profile environment:'UAT', target:'Info.plist', source:'Profiles/Info-UAT.plist'
      }

      xandroid { // Optional
        // General configuration
        automaticConfiguration = false
        solutionFile = file('App.sln')
        projectFile = file('App.Android/App.Android.csproj')
        projectName = 'App.Android'

        // A list of environments mapped to targets
        environment name:'DEV', configuration:'Release'
        environment name:'UAT', configuration:'Release'

        // A list of profiling actions for environments
        profile environment:'UAT', source: 'Profiles/TRAIN.groovy'
      }

    }

----
