#!/usr/bin/env bash

# DO NOT RUN IT AS IT IS.
# IT's just notes
# Only Work for linux

sudo dnf install java-21-openjdk-jmods java-21-openjdk java-21-openjdk-devel
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
jlink \
  --module-path "$JAVA_HOME/jmods:/home/michal/javafx-jmods-21.0.8" \
  --add-modules java.xml,java.logging,java.scripting,javafx.base,javafx.graphics,javafx.controls,javafx.fxml \
  --output custom-runtime \
  --strip-debug --no-man-pages --no-header-files --compress=2


jpackage \
  --name NetworkingNemo \
  --input target \
  --main-jar Networking-Nemo-1.0-SNAPSHOT.jar \
  --main-class nemo.networking.Nemo \
  --type app-image \
  --runtime-image ../custom-runtime \
  --dest dist


