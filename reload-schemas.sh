#!/usr/bin/env bash

curl -X POST $1/tasks/create-oracle-schema
curl -X POST $1/tasks/load-repositories