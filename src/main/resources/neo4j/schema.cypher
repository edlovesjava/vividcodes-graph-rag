// Neo4j Database Schema and Indexes for Graph RAG System
// This file contains the schema definitions and index creation statements

// ========================================
// INDEX CREATION
// ========================================

// Repository indexes
CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.id);
CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.name);
CREATE INDEX IF NOT EXISTS FOR (r:Repository) ON (r.organization);

// SubProject indexes
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.id);
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.name);
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.path);
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.type);
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.repository_id);

// Package indexes
CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.id);
CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.name);
CREATE INDEX IF NOT EXISTS FOR (p:Package) ON (p.path);

// Class indexes
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.id);
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.name);
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.package_name);
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.repository_id);

// Method indexes
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.id);
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.name);
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.class_name);
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.visibility);

// Field indexes
CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.id);
CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.name);
CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.class_name);

// ========================================
// CONSTRAINTS
// ========================================

// Unique constraints to ensure data integrity
CREATE CONSTRAINT IF NOT EXISTS FOR (r:Repository) REQUIRE r.id IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (sp:SubProject) REQUIRE sp.id IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (p:Package) REQUIRE p.id IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (c:Class) REQUIRE c.id IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (m:Method) REQUIRE m.id IS UNIQUE;
CREATE CONSTRAINT IF NOT EXISTS FOR (f:Field) REQUIRE f.id IS UNIQUE;

// ========================================
// COMPOSITE INDEXES
// ========================================

// Composite indexes for common query patterns
CREATE INDEX IF NOT EXISTS FOR (c:Class) ON (c.repository_id, c.package_name);
CREATE INDEX IF NOT EXISTS FOR (m:Method) ON (m.class_name, m.visibility);
CREATE INDEX IF NOT EXISTS FOR (sp:SubProject) ON (sp.repository_id, sp.type);

// ========================================
// FULL-TEXT SEARCH INDEXES
// ========================================

// Full-text search indexes for code search capabilities
CALL db.index.fulltext.createNodeIndex('classNameSearch', ['Class'], ['name']);
CALL db.index.fulltext.createNodeIndex('methodNameSearch', ['Method'], ['name']);
CALL db.index.fulltext.createNodeIndex('packageNameSearch', ['Package'], ['name']);
CALL db.index.fulltext.createNodeIndex('subProjectNameSearch', ['SubProject'], ['name', 'description']);

// ========================================
// SCHEMA DOCUMENTATION
// ========================================

/*
Node Types:
- Repository: Represents a Git repository or local project
- SubProject: Represents a sub-project within a repository (Maven modules, Gradle sub-projects, etc.)
- Package: Represents a Java package
- Class: Represents a Java class, interface, enum, or annotation
- Method: Represents a Java method or constructor
- Field: Represents a Java field or constant

Relationship Types:
- CONTAINS: Hierarchical containment (Repository->SubProject, SubProject->Package, Package->Class, etc.)
- BELONGS_TO: Reverse containment for easier queries
- CALLS: Method call relationships
- EXTENDS: Class inheritance
- IMPLEMENTS: Interface implementation
- DEPENDS_ON: Cross-project dependencies
- SHARES_WITH: Shared code relationships
- IMPORTS_FROM: Import relationships

Key Properties:
- All nodes have: id (unique), name, created_at, updated_at
- Code nodes have: file_path, line_start, line_end
- Repository nodes have: url, organization, branch, commit_hash
- SubProject nodes have: type (maven/gradle/npm/custom), path, buildFile
- Class nodes have: visibility, modifiers, package_name
- Method nodes have: visibility, return_type, parameters
*/
