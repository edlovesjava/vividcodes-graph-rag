#!/bin/bash

# Conventional Commit Message Validator
# This script validates that commit messages follow the conventional commits specification

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

# Get the commit message from the first argument or from stdin
if [ $# -eq 1 ]; then
    COMMIT_MSG="$1"
else
    COMMIT_MSG=$(cat)
fi

# Remove comments and empty lines
COMMIT_MSG=$(echo "$COMMIT_MSG" | sed '/^#/d' | sed '/^$/d' | head -1)

echo "🔍 Validating commit message: $COMMIT_MSG"

# Conventional commit regex pattern
# Format: <type>[optional scope]: <description>
# Types: feat, fix, docs, style, refactor, perf, test, chore, ci, build, revert
# Breaking changes: type! or BREAKING CHANGE in body
# Support multiple scopes: feat(scope1,scope2): description
CONVENTIONAL_PATTERN='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build|revert)(\([a-z-]+(,[a-z-]+)*\))?(!)?: .{1,72}$'

# Check if commit message matches conventional format
if [[ $COMMIT_MSG =~ $CONVENTIONAL_PATTERN ]]; then
    print_success "Commit message follows conventional format"
else
    print_error "Commit message does not follow conventional format"
    echo ""
    print_info "Expected format: <type>[optional scope]: <description>"
    echo ""
    print_info "Valid types: feat, fix, docs, style, refactor, perf, test, chore, ci, build, revert"
    echo ""
    print_info "Examples:"
    echo "  feat: add new feature"
    echo "  fix(parser): resolve parsing issue"
    echo "  docs(api): update endpoint documentation"
    echo "  chore(deps): update dependencies"
    echo "  feat(api)!: breaking change"
    echo ""
    print_info "For more details, see: docs/CONVENTIONAL_COMMITS.md"
    exit 1
fi

# Additional validations
TYPE=$(echo "$COMMIT_MSG" | sed -E 's/^([a-z]+).*/\1/')
SCOPE=$(echo "$COMMIT_MSG" | sed -E 's/^[a-z]+\(([a-z-]+)\):.*/\1/' 2>/dev/null || echo "")

# Validate scope if present
if [ -n "$SCOPE" ]; then
    VALID_SCOPES="parser graph api config test docs deps"
    if [[ ! " $VALID_SCOPES " =~ " $SCOPE " ]]; then
        print_warning "Scope '$SCOPE' is not in the standard list"
        print_info "Standard scopes: $VALID_SCOPES"
        print_info "Custom scopes are allowed but consider using standard ones"
    fi
fi

# Check for breaking change indicators
if [[ $COMMIT_MSG =~ \! ]] || [[ $COMMIT_MSG =~ BREAKING\ CHANGE ]]; then
    print_warning "Breaking change detected - ensure this is intentional"
fi

# Check description length
DESCRIPTION=$(echo "$COMMIT_MSG" | sed -E 's/^[a-z]+(\([a-z-]+\))?(!)?: (.+)/\3/')
if [ ${#DESCRIPTION} -gt 72 ]; then
    print_warning "Description is longer than 72 characters (${#DESCRIPTION} chars)"
    print_info "Consider using the commit body for detailed explanations"
fi

# Check for common mistakes
if [[ $COMMIT_MSG =~ \.$ ]]; then
    print_warning "Description ends with a period - conventional commits don't use periods"
fi

if [[ $COMMIT_MSG =~ [A-Z] ]]; then
    # Check if it's just the first letter or if there are other capitals
    FIRST_WORD=$(echo "$COMMIT_MSG" | sed -E 's/^[a-z]+(\([a-z-]+\))?(!)?: ([A-Z][a-z]+).*/\3/')
    if [[ $FIRST_WORD =~ ^[A-Z][a-z]+$ ]]; then
        print_success "Description starts with capital letter"
    else
        print_warning "Description contains unexpected capital letters"
        print_info "Use sentence case for descriptions"
    fi
fi

print_success "Commit message validation completed"
exit 0 