#!/usr/bin/env sh
BIN_DIR="$(dirname "$0")/build/install/compiler/bin"
$BIN_DIR/compiler "$1" "out1.s"
echo "Exit Code: $?"
set -e
as -o out1.o out1.s
ld -o "$2" out1.o
$BIN_DIR/compiler "$1" "out1.s"
