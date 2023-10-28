# JavaVersionManager

---
***JavaVersionManager*** is a simple tool to manage multiple JDK versions your system. It allows you to easily switch between versions of Java.



## How To Use

---
Use **jvm install** to install a jdk
```bash
    $ jvm install <version>
```
Use **jvm list** to list all installed jdks
```bash
    $ jvm list
```
Use **jvm list** remote to list all available jdks to install
```bash
    $ jvm list remote
```

Use **jvm default** to go back to your default jdk
```bash
    $ jvm default
```

Use **jvm current** to see what jdk you are currently using
```bash
    $ jvm current
```

## How To Build

---
To compile JVM you need at least JDK 8, and an internet connection.
(Be aware that the lowest jdk version you can install is the version you are using to compile JVM)

To install JavaVersionManager, you can use the standalone installer script. This script will download the latest version of the JVM and install it to your system. The script will also add the JVM to your PATH variable so you can use it from anywhere.

Quick Install
```bash
    $ wget https://raw.githubusercontent.com/Vantrex/JavaVersionManager/master/standalone-installer/install-jvm.sh && chmod 777 install-jvm.sh && ./install-jvm.sh
```
