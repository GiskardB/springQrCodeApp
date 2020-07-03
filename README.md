# Spring QrCode Application
[![Build Status](https://travis-ci.com/GiskardB/springQrCodeApp.svg?branch=master)](https://travis-ci.com/GiskardB/springQrCodeApp)
[![License](http://img.shields.io/:license-mit-blue.svg)](https://github.com/GiskardB/springQrCodeApp/blob/master/LICENSE)
[![Deployed on Heroku](https://img.shields.io/badge/heroku-deployed-blueviolet.svg?logo=heroku&)](https://qrcode-spring.herokuapp.com/)
[![Pushed to Docker Hub](https://img.shields.io/badge/docker_hub-released-blue.svg?logo=docker)](https://hub.docker.com/r/giskardocker80/qrcode-spring)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)

Example of Application to generate QRCode with Spring Boot WebFlux deployed on Heroku with Travis
It will help you go through the modernist deployment with some simple usage of Docker and Travis CI.

# Dependencies
install the following stuff before you start
- Java SDK (oraclejdk8/openjdk8)
- Maven
- Docker (Optional)

# Folder Structure
```
├── Dockerfile
├── LICENSE
├── README.md
├── pom.xml # application dependency and setup
└── src # your source folder
    ├── main
    │   └── java
    │       └── ...
    │   └── resources
    │       └── application.yml # Configuration for the application
    └── test
        └── java
            └── ...
```

# Start Locally
After launch the start command you can see the home page of QRCode application

![https://qrcode-spring.herokuapp.com/](screenshots/home.png) 

## I. Without Docker
```shell
1. Build jar (maven)
$ mvn clean install

2. Run jar locally
$ java -jar target/*.jar

3. Open browser and access localhost:8080
http://localhost:8080

4. You shall see the home page of QrCode Application
```

## II. With Docker
```shell
# This command will build the application
$ docker build . image_name

# and run it locally on port ${PORT}
Ex.> docker run -e "PORT=8080" -p 8080:8080 image_name

# Access localhost:${PORT}
http://localhost:${PORT}

```

# Basic use and Configuration

The application expose a REST Service on path **http://{ADDRESS}/qrcode**

At the moment, the inputs available are:
> - *text* : Message to encode to QRCode image
> -
> - *other*: todo.... 

Example: <http://localhost:8080/qrcode?text=WhatEverYouWant>


In the application.yml, you can customize the default parameters:
```shell
qrcode:
  logo:
    enabled: true # Enable the logo in the center of QRCode
    path: "classpath:/logo.jpg" # Absolute path, url or classpath of the logo image to embedded 
    size: 50    # Size of logo in pixel
    transparency: 1.0f  # Trasparency of logo between 0 and 1
  margin: 1 # Internal margin of the QrCode Image
  size: 300 # Size of QRCode
  rgbColorBackground: "#FFFFFF"  # Background color of QrCode
  rgbColorForeground: "#000000"  # Foreground color of QrCode
```


