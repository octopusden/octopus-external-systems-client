#!/bin/bash

if ! su git bash -c "gitea admin user create --username test-admin --password test-admin --email=admin@example.com --admin"; then
  exit 1
fi

exit 0
