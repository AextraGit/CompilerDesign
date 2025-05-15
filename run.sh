#!/usr/bin/env sh
BIN_DIR="$(dirname "$0")/build/install/compiler/bin"
$BIN_DIR/compiler "$@"
set -e
as -o out1.o "$2"
ld -o "$2" out1.o