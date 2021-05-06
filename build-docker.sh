#!/bin/bash
docker build --tag=vse_verification_bot .
docker run --rm --name=vse_verification_bot -it vse_verification_bot
