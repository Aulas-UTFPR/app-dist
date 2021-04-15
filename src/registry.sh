#!/bin/bash

sudo sysctl -w net.ipv6.conf.all.disable_ipv6=1
pkill rmiregistry
cd ../bin/
rmiregistry &
