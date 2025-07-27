#!/bin/bash

# Pre-commit hook for Java Graph RAG project
# This script runs quality checks before allowing a commit

set -e

echo "🔍 Running pre-commit quality checks..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found. Please run this script from the project root."
    exit 1
fi

# 1. Run Maven compile
echo "📦 Compiling project..."
if mvn compile -q; then
    print_status "Compilation successful"
else
    print_error "Compilation failed"
    exit 1
fi

# 2. Run tests
echo "🧪 Running tests..."
if mvn test -q; then
    print_status "All tests passed"
else
    print_error "Tests failed"
    exit 1
fi

# 3. Run Checkstyle
echo "📋 Running code style checks..."
if mvn checkstyle:check -q; then
    print_status "Code style checks passed"
else
    print_error "Code style violations found"
    print_warning "Run 'mvn checkstyle:check' to see details"
    exit 1
fi

# 4. Run SpotBugs
echo "🐛 Running bug detection..."
if mvn spotbugs:check -q; then
    print_status "No bugs detected"
else
    print_warning "Potential bugs found (build continues)"
    print_info "Run 'mvn spotbugs:check' to see details"
    print_info "SpotBugs issues are warnings and don't fail the build"
fi

# 5. Check for TODO/FIXME comments
echo "📝 Checking for TODO/FIXME comments..."
TODO_COUNT=$(grep -r "TODO\|FIXME" src/ --include="*.java" | wc -l)
if [ "$TODO_COUNT" -eq 0 ]; then
    print_status "No TODO/FIXME comments found"
else
    print_warning "Found $TODO_COUNT TODO/FIXME comments"
    grep -r "TODO\|FIXME" src/ --include="*.java" || true
fi

# 6. Check for console.log or System.out.println
echo "🔇 Checking for debug statements..."
DEBUG_COUNT=$(grep -r "System\.out\.println\|console\.log" src/ --include="*.java" | wc -l)
if [ "$DEBUG_COUNT" -eq 0 ]; then
    print_status "No debug statements found"
else
    print_warning "Found $DEBUG_COUNT debug statements"
    grep -r "System\.out\.println\|console\.log" src/ --include="*.java" || true
fi

# 7. Check file sizes
echo "📏 Checking file sizes..."
LARGE_FILES=$(find src/ -name "*.java" -size +50k | wc -l)
if [ "$LARGE_FILES" -eq 0 ]; then
    print_status "No excessively large files found"
else
    print_warning "Found $LARGE_FILES files larger than 50KB"
    find src/ -name "*.java" -size +50k || true
fi

# 8. Check for proper imports
echo "📚 Checking imports..."
if mvn compile -q 2>&1 | grep -q "unused import\|unused imports"; then
    print_warning "Unused imports detected"
    print_warning "Run 'mvn compile' to see details"
else
    print_status "Import checks passed"
fi

# 9. Validate YAML configuration
echo "⚙️  Validating configuration files..."
if command -v yamllint >/dev/null 2>&1; then
    if yamllint src/main/resources/application.yml; then
        print_status "YAML configuration is valid"
    else
        print_error "YAML configuration has issues"
        exit 1
    fi
else
    print_warning "yamllint not found, skipping YAML validation"
fi

# 10. Check for sensitive data in configuration
echo "🔒 Checking for sensitive data..."
if grep -r "password\|secret\|key" src/main/resources/ --include="*.yml" --include="*.properties" | grep -v "spring.neo4j.authentication.password" | grep -v "NEO4J_PASSWORD"; then
    print_warning "Potential sensitive data found in configuration"
    print_warning "Review configuration files for hardcoded secrets"
else
    print_status "No obvious sensitive data found"
fi

echo ""
print_status "All pre-commit checks completed successfully!"
echo ""
echo "🚀 Ready to commit! Remember to:"
echo "   - Write a descriptive commit message"
echo "   - Reference any related issues"
echo "   - Update documentation if needed"
echo ""

exit 0 