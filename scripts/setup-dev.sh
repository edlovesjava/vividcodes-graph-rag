#!/bin/bash

# Development setup script for Java Graph RAG project
# This script sets up the development environment

set -e

echo "🚀 Setting up Java Graph RAG development environment..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
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

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    print_error "pom.xml not found. Please run this script from the project root."
    exit 1
fi

# 1. Check Java version
echo "☕ Checking Java version..."
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -ge 17 ]; then
    print_status "Java $JAVA_VERSION detected (minimum required: 17)"
else
    print_error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

# 2. Check Maven
echo "📦 Checking Maven..."
if command -v mvn >/dev/null 2>&1; then
    MVN_VERSION=$(mvn -version | head -n 1 | cut -d' ' -f3)
    print_status "Maven $MVN_VERSION detected"
else
    print_error "Maven is not installed"
    exit 1
fi

# 3. Check Docker
echo "🐳 Checking Docker..."
if command -v docker >/dev/null 2>&1; then
    print_status "Docker detected"
    if docker info >/dev/null 2>&1; then
        print_status "Docker is running"
    else
        print_warning "Docker is not running. Start Docker to use Neo4j."
    fi
else
    print_warning "Docker not found. Install Docker to use Neo4j container."
fi

# 4. Check Docker Compose
echo "🐙 Checking Docker Compose..."
if command -v docker-compose >/dev/null 2>&1; then
    print_status "Docker Compose detected"
else
    print_warning "Docker Compose not found. Install Docker Compose for easy Neo4j setup."
fi

# 5. Install Git hooks
echo "🔗 Setting up Git hooks..."
if [ -d ".git" ]; then
    # Create hooks directory if it doesn't exist
    mkdir -p .git/hooks

    # Copy pre-commit hook
    cp scripts/pre-commit.sh .git/hooks/pre-commit
    chmod +x .git/hooks/pre-commit
    print_status "Pre-commit hook installed"

    # Copy commit-msg hook
    cp scripts/commit-msg.sh .git/hooks/commit-msg
    chmod +x .git/hooks/commit-msg
    print_status "Commit-msg hook installed"
else
    print_warning "Not a Git repository. Initialize Git to use hooks."
fi

# 6. Download dependencies
echo "📚 Downloading Maven dependencies..."
if mvn dependency:resolve -q; then
    print_status "Dependencies downloaded successfully"
else
    print_error "Failed to download dependencies"
    exit 1
fi

# 7. Compile project
echo "🔨 Compiling project..."
if mvn compile -q; then
    print_status "Project compiled successfully"
else
    print_error "Compilation failed"
    exit 1
fi

# 8. Run tests
echo "🧪 Running tests..."
if mvn test -q; then
    print_status "All tests passed"
else
    print_error "Tests failed"
    exit 1
fi

# 9. Check code style
echo "📋 Running code style checks..."
if mvn checkstyle:check -q; then
    print_status "Code style checks passed"
else
    print_warning "Code style violations found"
    print_info "Run 'mvn checkstyle:check' to see details"
fi

# 10. Create development configuration
echo "⚙️  Setting up development configuration..."
if [ ! -f "src/main/resources/application-dev.yml" ]; then
    cat > src/main/resources/application-dev.yml << EOF
spring:
  profiles: dev
  neo4j:
    uri: bolt://localhost:7687
    authentication:
      username: neo4j
      password: password

logging:
  level:
    com.vividcodes.graphrag: DEBUG
    org.neo4j: INFO
    org.springframework: INFO

server:
  port: 8080
EOF
    print_status "Development configuration created"
else
    print_status "Development configuration already exists"
fi

# 11. Create IDE configuration
echo "💻 Setting up IDE configuration..."
if [ ! -d ".vscode" ]; then
    mkdir -p .vscode
    cat > .vscode/settings.json << EOF
{
    "java.configuration.updateBuildConfiguration": "automatic",
    "java.compile.nullAnalysis.mode": "automatic",
    "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
    "java.format.settings.profile": "GoogleStyle",
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.organizeImports": true
    },
    "files.exclude": {
        "**/target": true,
        "**/.classpath": true,
        "**/.project": true,
        "**/.settings": true,
        "**/.factorypath": true
    }
}
EOF
    print_status "VS Code configuration created"
fi

# 12. Create IntelliJ IDEA configuration
if [ ! -d ".idea" ]; then
    mkdir -p .idea
    cat > .idea/codeStyles/Project.xml << EOF
<component name="ProjectCodeStyleConfiguration">
  <code_scheme name="Project" version="173">
    <JavaCodeStyleSettings>
      <option name="IMPORT_LAYOUT_TABLE">
        <value>
          <package name="java" withSubpackages="true" static="false" />
          <package name="javax" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="org" withSubpackages="true" static="false" />
          <package name="com" withSubpackages="true" static="false" />
          <emptyLine />
          <package name="" withSubpackages="true" static="false" />
        </value>
      </option>
    </JavaCodeStyleSettings>
  </code_scheme>
</component>
EOF
    print_status "IntelliJ IDEA configuration created"
fi

echo ""
print_status "Development environment setup completed!"
echo ""
echo "🎯 Next steps:"
echo "   1. Start Neo4j: docker-compose up neo4j -d"
echo "   2. Run application: mvn spring-boot:run"
echo "   3. Test API: curl http://localhost:8080/api/v1/health"
echo "   4. Open Neo4j Browser: http://localhost:7474"
echo ""
echo "📚 Useful commands:"
echo "   - Build: mvn clean install"
echo "   - Test: mvn test"
echo "   - Run: mvn spring-boot:run"
echo "   - Check style: mvn checkstyle:check"
echo "   - Find bugs: mvn spotbugs:check"
echo ""

exit 0 