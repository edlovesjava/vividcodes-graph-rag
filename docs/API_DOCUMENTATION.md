# API Documentation

## Overview

The Java Graph RAG system provides REST API endpoints for code ingestion, health monitoring, and graph querying. All endpoints return JSON responses and use standard HTTP status codes.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. Future versions may include API key or OAuth2 authentication.

## Common Response Format

### Success Response
```json
{
  "status": "success",
  "message": "Operation completed successfully",
  "data": { ... }
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "timestamp": "2025-07-25T18:30:00Z"
}
```

## Endpoints

### 1. Health Check

#### GET /health

Check the health status of the application and its dependencies.

**Request:**
```http
GET /api/v1/health
```

**Response:**
```json
{
  "status": "UP",
  "service": "Java Graph RAG",
  "version": "0.1.0-SNAPSHOT",
  "neo4j": {
    "status": "UP",
    "version": "5.15.0"
  }
}
```

**Status Codes:**
- `200 OK`: Application is healthy
- `503 Service Unavailable`: Application or dependencies are unhealthy

**Example:**
```bash
curl http://localhost:8080/api/v1/health
```

### 2. Code Ingestion

#### POST /ingest

Ingest Java source code into the graph database.

**Request:**
```http
POST /api/v1/ingest
Content-Type: application/json
```

**Request Body:**
```json
{
  "sourcePath": "/path/to/java/project",
  "filters": {
    "includePrivate": false,
    "includeTests": false,
    "filePatterns": ["*.java"]
  }
}
```

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `sourcePath` | String | Yes | Path to Java source code directory or file |
| `filters.includePrivate` | Boolean | No | Include private members (default: false) |
| `filters.includeTests` | Boolean | No | Include test files (default: false) |
| `filters.filePatterns` | Array<String> | No | File patterns to include (default: ["*.java"]) |

**Response:**
```json
{
  "status": "success",
  "message": "Code ingestion completed successfully",
  "sourcePath": "/path/to/java/project",
  "statistics": {
    "filesProcessed": 10,
    "classesFound": 25,
    "methodsFound": 150,
    "fieldsFound": 75
  }
}
```

**Status Codes:**
- `200 OK`: Ingestion completed successfully
- `400 Bad Request`: Invalid request parameters
- `404 Not Found`: Source path does not exist
- `500 Internal Server Error`: Processing error

**Example:**
```bash
curl -X POST http://localhost:8080/api/v1/ingest \
  -H "Content-Type: application/json" \
  -d '{
    "sourcePath": "/path/to/your/java/project",
    "filters": {
      "includePrivate": false,
      "includeTests": false
    }
  }'
```

### 3. Graph Query (Phase 2)

#### POST /query

Execute natural language queries against the code graph.

**Request:**
```http
POST /api/v1/query
Content-Type: application/json
```

**Request Body:**
```json
{
  "query": "Find all public methods that call the database",
  "context": {
    "maxResults": 10,
    "includeCode": true
  }
}
```

**Response:**
```json
{
  "status": "success",
  "query": "Find all public methods that call the database",
  "results": [
    {
      "nodeId": "method:com.example:UserService:saveUser",
      "nodeType": "Method",
      "relevance": 0.95,
      "codeContext": {
        "filePath": "/src/main/java/com/example/UserService.java",
        "lineStart": 45,
        "lineEnd": 55,
        "code": "public User saveUser(User user) {\n    return userRepository.save(user);\n}"
      }
    }
  ]
}
```

## Error Handling

### Validation Errors

When request validation fails:

```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    {
      "field": "sourcePath",
      "message": "Source path is required"
    }
  ]
}
```

### File System Errors

When the source path is invalid:

```json
{
  "status": "error",
  "message": "Source path does not exist: /invalid/path",
  "errorCode": "FILE_NOT_FOUND"
}
```

### Processing Errors

When code parsing fails:

```json
{
  "status": "error",
  "message": "Failed to parse Java file: /path/to/file.java",
  "errorCode": "PARSING_ERROR",
  "details": "Syntax error at line 25"
}
```

## Rate Limiting

Currently, no rate limiting is implemented. Future versions may include:
- Request rate limiting per IP
- Concurrent request limits
- API usage quotas

## CORS Configuration

The API supports CORS for web-based clients:

```javascript
// Example CORS request
fetch('http://localhost:8080/api/v1/health', {
  method: 'GET',
  headers: {
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

## SDK Examples

### Java Client Example

```java
import org.springframework.web.client.RestTemplate;

RestTemplate restTemplate = new RestTemplate();

// Health check
String healthUrl = "http://localhost:8080/api/v1/health";
HealthResponse health = restTemplate.getForObject(healthUrl, HealthResponse.class);

// Code ingestion
String ingestUrl = "http://localhost:8080/api/v1/ingest";
IngestionRequest request = new IngestionRequest();
request.setSourcePath("/path/to/java/project");

IngestionResponse response = restTemplate.postForObject(ingestUrl, request, IngestionResponse.class);
```

### Python Client Example

```python
import requests

# Health check
response = requests.get('http://localhost:8080/api/v1/health')
health_data = response.json()

# Code ingestion
ingest_data = {
    "sourcePath": "/path/to/java/project",
    "filters": {
        "includePrivate": False,
        "includeTests": False
    }
}

response = requests.post('http://localhost:8080/api/v1/ingest', json=ingest_data)
result = response.json()
```

### JavaScript/Node.js Client Example

```javascript
const axios = require('axios');

// Health check
const healthResponse = await axios.get('http://localhost:8080/api/v1/health');
console.log(healthResponse.data);

// Code ingestion
const ingestData = {
  sourcePath: '/path/to/java/project',
  filters: {
    includePrivate: false,
    includeTests: false
  }
};

const response = await axios.post('http://localhost:8080/api/v1/ingest', ingestData);
console.log(response.data);
```

## API Versioning

The API uses URL versioning (`/api/v1/`). Future versions will be available at `/api/v2/`, `/api/v3/`, etc.

### Version Compatibility

- **v1.0**: Initial release with code ingestion and health checks
- **v1.1**: Added graph querying capabilities (Phase 2)
- **v2.0**: Breaking changes for enhanced features

## OpenAPI/Swagger Documentation

When the application is running, you can access the interactive API documentation at:

```
http://localhost:8080/swagger-ui.html
```

This provides:
- Interactive API testing
- Request/response examples
- Schema documentation
- Try-it-out functionality

## Monitoring and Metrics

### Health Check Endpoints

- `/actuator/health`: Detailed health information
- `/actuator/info`: Application information
- `/actuator/metrics`: Application metrics

### Logging

API requests are logged with:
- Request timestamp
- HTTP method and path
- Response status code
- Processing time
- Error details (if applicable)

Example log entry:
```
2025-07-25 18:30:15.123 INFO  [http-nio-8080-exec-1] c.v.g.controller.IngestionController - 
POST /api/v1/ingest - 200 OK - 1250ms
``` 