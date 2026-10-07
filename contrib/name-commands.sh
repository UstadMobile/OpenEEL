#!/bin/bash

#find . -name build.gradle.kts | xargs sed -i 's/world.respect/org.openeel/g'

#find . -name \*.kt | xargs sed -i 's/package world.respect/package org.openeel/g'
#find . -name \*.kt | xargs sed -i 's/import world.respect/import org.openeel/g'

DIRS=$(ls -l)
ROOT_DIR=$(pwd)

for MOD_DIR in $(ls -l); do
    MOD_SRC_DIR="$MOD_DIR/src"
    cd $ROOT_DIR

    if [ -e $ROOT_DIR/$MOD_SRC_DIR ]; then
        for SRC_SET_NAME in java commonMain androidMain jvmMain jvmTest androidTest main; do
            if [ -e $ROOT_DIR/$MOD_SRC_DIR/$SRC_SET_NAME ]; then
                for LANG_NAME in java kotlin; do
                    LANG_EEL_DIR="$ROOT_DIR/$MOD_SRC_DIR/$SRC_SET_NAME/$LANG_NAME/org/openeel"
                    WORLD_DIR="$ROOT_DIR/$MOD_SRC_DIR/$SRC_SET_NAME/$LANG_NAME/world/respect"
                    if [ -e $WORLD_DIR ]; then
                        echo "Moving $WORLD_DIR -> $LANG_EEL_DIR"

                        if [ ! -e $LANG_EEL_DIR ]; then
                            echo mkdir $LANG_EEL_DIR
                            mkdir -p $LANG_EEL_DIR
                        fi

                        mv $WORLD_DIR/* $LANG_EEL_DIR
                        rmdir $WORLD_DIR
                        rmdir $ROOT_DIR/$MOD_SRC_DIR/$SRC_SET_NAME/$LANG_NAME/world/
                    fi
                done
            fi
        done
    fi
done
