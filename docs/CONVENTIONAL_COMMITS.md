# Conventional Commits Guide

This project follows the [Conventional Commits](https://www.conventionalcommits.org/) specification for commit messages. This ensures consistent, readable commit history and enables automated tools for versioning and changelog generation.

## Commit Message Format

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Structure

- **Type**: Required. Describes the kind of change (feat, fix, docs, etc.)
- **Scope**: Optional. Indicates the part of the codebase affected
- **Description**: Required. Short description in present tense
- **Body**: Optional. Detailed explanation of the change
- **Footer**: Optional. References to issues, breaking changes, etc.

## Commit Types

### Primary Types

| Type       | Description                                               | Example                                           |
| ---------- | --------------------------------------------------------- | ------------------------------------------------- |
| `feat`     | A new feature                                             | `feat: add Java code parsing functionality`       |
| `fix`      | A bug fix                                                 | `fix: resolve Neo4j authentication issue`         |
| `docs`     | Documentation changes                                     | `docs: update API documentation`                  |
| `style`    | Code style changes (formatting, missing semicolons, etc.) | `style: format code according to checkstyle`      |
| `refactor` | Code refactoring (no functional changes)                  | `refactor: extract method for better readability` |
| `perf`     | Performance improvements                                  | `perf: optimize database queries`                 |
| `test`     | Adding or updating tests                                  | `test: add integration tests for parser service`  |
| `chore`    | Build process, tooling, or maintenance changes            | `chore: update Maven dependencies`                |

### Additional Types

| Type     | Description                           | Example                                           |
| -------- | ------------------------------------- | ------------------------------------------------- |
| `ci`     | CI/CD pipeline changes                | `ci: add GitHub Actions workflow`                 |
| `build`  | Build system or external dependencies | `build: upgrade to Java 17`                       |
| `revert` | Revert a previous commit              | `revert: revert "feat: add experimental feature"` |

## Scopes

Use scopes to indicate which part of the codebase is affected:

| Scope    | Description               | Example                                           |
| -------- | ------------------------- | ------------------------------------------------- |
| `parser` | Java parser functionality | `feat(parser): add support for enum parsing`      |
| `graph`  | Graph database operations | `fix(graph): resolve relationship creation issue` |
| `api`    | REST API endpoints        | `feat(api): add health check endpoint`            |
| `config` | Configuration files       | `fix(config): correct Neo4j connection settings`  |
| `test`   | Test-related changes      | `test(parser): add unit tests for method parsing` |
| `docs`   | Documentation             | `docs(api): add endpoint examples`                |
| `deps`   | Dependencies              | `chore(deps): update Spring Boot to 3.2.0`        |

## Examples

### Simple Fix

```
fix: resolve Neo4j authentication error
```

### Feature with Scope

```
feat(parser): add support for interface parsing

- Parse interface declarations
- Extract interface methods
- Create interface nodes in graph database

Closes #123
```

### Breaking Change

```
feat(api)!: change response format for health endpoint

BREAKING CHANGE: Health endpoint now returns detailed status object instead of simple string.

The response format has changed from:
{
  "status": "UP"
}

To:
{
  "service": "Java Graph RAG",
  "version": "0.1.0",
  "neo4j": {
    "status": "UP",
    "version": "5.15.0"
  }
}
```

### Multiple Scopes

```
fix(parser,graph): resolve class relationship creation

- Fix issue where class relationships weren't being created
- Update parser to properly handle nested classes
- Ensure graph service creates CONTAINS relationships
```

### Revert

```
revert: feat(parser): add experimental annotation parsing

This reverts commit abc123def456.
```

## Breaking Changes

Use `!` after the type/scope to indicate breaking changes:

```
feat(api)!: change authentication method

BREAKING CHANGE: API now requires Bearer token authentication instead of API key.
```

## Issue References

Reference issues in the footer:

```
feat: add user authentication

Closes #123
Fixes #456
Relates to #789
```

## Commit Message Guidelines

### Do's

- ✅ Use present tense ("add" not "added")
- ✅ Use imperative mood ("move" not "moves")
- ✅ Keep description under 72 characters
- ✅ Use body for detailed explanations
- ✅ Reference issues when applicable
- ✅ Use appropriate type and scope

### Don'ts

- ❌ Don't end description with a period
- ❌ Don't use past tense
- ❌ Don't make description too long
- ❌ Don't skip the type
- ❌ Don't use vague descriptions

## Tools and Automation

### Commitizen

Install Commitizen for interactive commit creation:

```bash
npm install -g commitizen
npm install -g cz-conventional-changelog
```

### Pre-commit Hook

The project includes a pre-commit hook that validates commit messages.

### Conventional Changelog

Generate changelogs automatically:

```bash
npm install -g conventional-changelog-cli
conventional-changelog -p angular -i CHANGELOG.md -s
```

## Project-Specific Examples

### Java Graph RAG Examples

```
feat(parser): implement Java class parsing
feat(graph): add Neo4j node creation for classes
feat(api): add code ingestion endpoint
fix(parser): resolve method parameter parsing issue
fix(graph): fix relationship creation between classes
docs(api): add OpenAPI documentation
test(parser): add unit tests for package parsing
chore(deps): update JavaParser to 3.27.0
ci: add GitHub Actions workflow
build: upgrade to Java 17
style: apply checkstyle formatting
refactor(service): extract graph operations to separate service
perf(graph): optimize database queries with batching
```

### Breaking Changes in This Project

```
feat(api)!: change ingestion request format
feat(graph)!: modify node property structure
feat(parser)!: change method signature parsing
```

## Integration with Versioning

Conventional commits enable automatic versioning:

- `feat` commits trigger minor version bumps
- `fix` commits trigger patch version bumps
- Breaking changes trigger major version bumps

## Resources

- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Angular Commit Guidelines](https://github.com/angular/angular/blob/main/CONTRIBUTING.md#-commit-message-format)
- [Commitizen](http://commitizen.github.io/cz-cli/)
- [Conventional Changelog](https://github.com/conventional-changelog/conventional-changelog)
