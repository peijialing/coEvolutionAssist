#!/usr/bin/env bash
for arg in "$*"
do
     echo $arg
done
for arg in "$*"
do
    grep $arg -ir . > ./scanRes.txt
done
