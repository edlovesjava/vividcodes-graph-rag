#!/bin/bash

# Commit Message Hook
# This script validates commit messages to ensure they follow conventional commits format

set -e

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Get the commit message file path
COMMIT_MSG_FILE="$1"

if [ ! -f "$COMMIT_MSG_FILE" ]; then
    print_error "Commit message file not found: $COMMIT_MSG_FILE"
    exit 1
fi

echo "🔍 Validating commit message..."

# Validate the commit message
if ! ./scripts/validate-commit-msg.sh < "$COMMIT_MSG_FILE"; then
    print_error "Commit message validation failed"
    print_info "Please follow the conventional commits format"
    print_info "See docs/CONVENTIONAL_COMMITS.md for details"
    exit 1
fi

print_success "Commit message validation passed"
exit 0 