# Plastique
[![CircleCI](https://circleci.com/gh/plastiqueapp/plastique.svg?style=svg&circle-token=7d60ca7b56d6385cc628847c599e0c3db42728f8)](https://circleci.com/gh/plastiqueapp/plastique)
![GitHub CI](https://github.com/plastiqueapp/plastique/workflows/Build/badge.svg?branch=dev)

Plastique in a work-in-progress open-source app for [DeviantArt](https://www.deviantart.com).

## Getting Started

Before building you'll need to register the app with DeviantArt.

1. Go to https://www.deviantart.com/developers/.
2. Login with your DeviantArt account. If you don't have one you can register for free.
3. Click **Register your Application**.
4. Enter arbitrary application name and for **OAuth2 Redirect URI Whitelist** enter the following:
```
io.plastique.android://auth
io.plastique.android.dev://auth
```
5. Click **Save**.
6. Run `tools/bootstrap.sh` from the command line. Copy and paste **client_id** and **client_secret** from the web page when prompted.

After you complete these steps you should be able to build the app from the command line or using the latest version of [Android Studio](https://developer.android.com/studio/).

## Contributing
If you have found a bug or want to request a new feature, open an issue.

## License

```
Copyright 2018 Sergey Chelombitko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
