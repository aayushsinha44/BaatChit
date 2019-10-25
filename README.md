# BaatChit

Tor Based Chat App for Android. 
This app doesnot requires to install any tor client on user mobile. 
The user just have to install this app and will itself connect to the tor network.
It contains all the tor binary to start tor network. 


## Getting Started

```
git clone https://github.com/aayushsinha44/BaatChit.git
cd BaatChit
```

After this load the project to android studio, build it and generate the apk for usage.

### Prerequisites

This complete project has been carried out in the following environment.

```
Go >= 1.12.6
Android Studio >= 3.5
GoMobile 
```

### Go Backend Setup

If you want to setup from sratch then install this go dependencies else move forward.

First install the following go dependencies.

```
go get -u github.com/cretz/bine/tor
go get -u -v -x github.com/ipsn/go-libtor
```

Then generate the .aar and .jar file form the source code of go-backend/

```
cd go-backend
gomobile bind -v -x .
```
This will generate the required file. Now load this files to your android projects.


### Working

On start this app takes sometime to connet with tor circuit. It will generate onion id for user. 
The user have to share this onion id to start chatting with anyone.


## Author

* **Aayush Sinha** - *Initial work* - [aayushsinha44](https://github.com/aayushsinha44)
