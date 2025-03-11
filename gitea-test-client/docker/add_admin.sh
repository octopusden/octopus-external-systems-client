#!/bin/bash
until gitea admin user list &> /dev/null; do sleep 5; done
gitea admin user create --username=test-admin --password=test-admin --email=admin@example.com --admin=true
