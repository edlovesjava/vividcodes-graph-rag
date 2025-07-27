# Development Scripts

This directory contains utility scripts for development, testing, and deployment of the Java Graph RAG project.

## 📁 Scripts Overview

### 🚀 Development Setup

- **[setup-dev.sh](setup-dev.sh)** - Complete development environment setup

### 🔍 Quality Assurance

- **[pre-commit.sh](pre-commit.sh)** - Pre-commit quality checks
- **[validate-commit-msg.sh](validate-commit-msg.sh)** - Conventional commit message validation
- **[commit-msg.sh](commit-msg.sh)** - Git commit-msg hook for commit validation

## 🛠️ Usage

### Initial Development Setup

Run this script once when setting up the project for the first time:

```bash
./scripts/setup-dev.sh
```

This script will:

- ✅ Check Java version (requires Java 17+)
- ✅ Verify Maven installation
- ✅ Check Docker and Docker Compose
- ✅ Install Git hooks
- ✅ Download Maven dependencies
- ✅ Compile the project
- ✅ Run all tests
- ✅ Perform code style checks
- ✅ Create development configuration
- ✅ Set up IDE configurations

### Pre-commit Quality Checks

The pre-commit hook runs automatically before each commit, but you can also run it manually:

```bash
./scripts/pre-commit.sh
```

This script performs:

- ✅ Project compilation
- ✅ Unit and integration tests
- ✅ Code style validation (Checkstyle)
- ✅ Bug detection (SpotBugs)
- ✅ TODO/FIXME comment detection
- ✅ Debug statement detection
- ✅ File size validation
- ✅ Import validation
- ✅ YAML configuration validation
- ✅ Sensitive data detection

### Commit Message Validation

The project enforces conventional commit message format. The commit-msg hook validates all commit messages automatically:

```bash
# Manual validation (optional)
./scripts/validate-commit-msg.sh "feat(parser): add java class parsing"
```

**Valid commit message format:**

```
<type>[optional scope]: <description>

[optional body]

[optional footer]
```

**Examples:**

- `feat: add new feature`
- `fix(parser): resolve parsing issue`
- `docs(api): update endpoint documentation`
- `chore(deps): update dependencies`
- `feat(api)!: breaking change`

For detailed guidelines, see **[docs/CONVENTIONAL_COMMITS.md](../docs/CONVENTIONAL_COMMITS.md)**.

## 🔧 Manual Quality Checks

### Code Style

```bash
mvn checkstyle:check
```

### Bug Detection

```bash
mvn spotbugs:check
```

### Test Coverage

```bash
mvn test jacoco:report
```

### Security Scan

```bash
mvn dependency:check
```

## 📋 Prerequisites

### Required Tools

- **Java 17+**: OpenJDK or Oracle JDK
- **Maven 3.8+**: Build and dependency management
- **Docker**: For Neo4j container (optional but recommended)
- **Git**: Version control

### Optional Tools

- **yamllint**: YAML validation (install via pip: `pip install yamllint`)
- **VS Code**: With Java extensions
- **IntelliJ IDEA**: With Java support

## 🐛 Troubleshooting

### Common Issues

#### Java Version Issues

```bash
# Check Java version
java -version

# Set JAVA_HOME if needed
export JAVA_HOME=/path/to/java17
```

#### Maven Issues

```bash
# Clean and rebuild
mvn clean install

# Update dependencies
mvn dependency:resolve
```

#### Docker Issues

```bash
# Check Docker status
docker info

# Start Docker service
sudo systemctl start docker
```

#### Git Hook Issues

```bash
# Reinstall hooks
cp scripts/pre-commit.sh .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

### Script Permissions

If scripts are not executable:

```bash
chmod +x scripts/*.sh
```

## 🔄 Continuous Integration

These scripts are designed to work with CI/CD pipelines:

### GitHub Actions Example

```yaml
name: Quality Checks
on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: "17"
      - run: ./scripts/setup-dev.sh
      - run: ./scripts/pre-commit.sh
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any

    stages {
        stage('Setup') {
            steps {
                sh './scripts/setup-dev.sh'
            }
        }
        stage('Quality') {
            steps {
                sh './scripts/pre-commit.sh'
            }
        }
    }
}
```

## 📊 Quality Metrics

The scripts help maintain these quality standards:

- **Code Coverage**: >80% line coverage
- **Code Style**: Checkstyle compliance
- **Bug Detection**: SpotBugs analysis
- **Security**: Dependency vulnerability scanning
- **Documentation**: API documentation completeness

## 🔧 Customization

### Adding New Checks

To add new quality checks to the pre-commit script:

1. Add the check logic to `pre-commit.sh`
2. Use appropriate exit codes (0 for success, 1 for failure)
3. Add descriptive output messages
4. Test the script thoroughly

### Modifying Checkstyle Rules

Edit `checkstyle.xml` in the project root to customize code style rules.

### Updating Maven Plugins

Modify `pom.xml` to add or update quality assurance plugins.

## 📚 Related Documentation

- **[Main README](../README.md)** - Project overview and quick start
- **[Architecture Documentation](../docs/ARCHITECTURE.md)** - System design
- **[API Documentation](../docs/API_DOCUMENTATION.md)** - REST API reference
- **[Deployment Guide](../docs/DEPLOYMENT.md)** - Production deployment

## 🤝 Contributing

When adding new scripts:

1. Follow the existing naming convention
2. Add proper error handling
3. Include colored output for better UX
4. Add documentation in this README
5. Test on multiple environments
6. Keep scripts focused and modular

## 📞 Support

For issues with the scripts:

1. Check the troubleshooting section above
2. Review the script output for error messages
3. Verify prerequisites are installed
4. Check file permissions
5. Create an issue in the repository

---

_These scripts are maintained as part of the Java Graph RAG project._
