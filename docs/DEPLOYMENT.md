# Deployment Documentation

## Overview

This document provides comprehensive deployment instructions for the Java Graph RAG system across different environments.

## Prerequisites

### System Requirements

- **Java**: OpenJDK 17 or higher
- **Maven**: 3.8+ for building
- **Docker**: 20.10+ for containerized deployment
- **Docker Compose**: 2.0+ for multi-container orchestration
- **Memory**: Minimum 4GB RAM (8GB recommended)
- **Disk Space**: 10GB+ for application and database

### Network Requirements

- **Ports**:
  - `8080`: Spring Boot application
  - `7474`: Neo4j HTTP interface
  - `7687`: Neo4j Bolt protocol

## Development Environment

### Local Development Setup

1. **Clone Repository**
   ```bash
   git clone <repository-url>
   cd vividcodes-graph-rag
   ```

2. **Start Neo4j Database**
   ```bash
   docker-compose up neo4j -d
   ```

3. **Build Application**
   ```bash
   mvn clean install
   ```

4. **Run Application**
   ```bash
   mvn spring-boot:run
   ```

5. **Verify Deployment**
   ```bash
   # Health check
   curl http://localhost:8080/api/v1/health
   
   # Neo4j browser
   open http://localhost:7474
   ```

### Development Configuration

**application-dev.yml**
```yaml
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

server:
  port: 8080
```

## Production Environment

### Docker Deployment

1. **Build Docker Image**
   ```bash
   docker build -t graph-rag:latest .
   ```

2. **Create Production Docker Compose**
   ```yaml
   # docker-compose.prod.yml
   version: '3.8'
   
   services:
     app:
       image: graph-rag:latest
       container_name: graph-rag-app
       environment:
         - SPRING_PROFILES_ACTIVE=prod
         - SPRING_NEO4J_URI=bolt://neo4j:7687
         - SPRING_NEO4J_AUTHENTICATION_USERNAME=neo4j
         - SPRING_NEO4J_AUTHENTICATION_PASSWORD=${NEO4J_PASSWORD}
       ports:
         - "8080:8080"
       depends_on:
         - neo4j
       restart: unless-stopped
       networks:
         - graph-rag-network
   
     neo4j:
       image: neo4j:5.15
       container_name: graph-rag-neo4j
       environment:
         - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD}
         - NEO4J_PLUGINS=["apoc"]
         - NEO4J_dbms_security_procedures_unrestricted=apoc.*
       ports:
         - "7474:7474"
         - "7687:7687"
       volumes:
         - neo4j_data:/data
         - neo4j_logs:/logs
         - neo4j_import:/var/lib/neo4j/import
         - neo4j_plugins:/plugins
       restart: unless-stopped
       networks:
         - graph-rag-network
   
   volumes:
     neo4j_data:
     neo4j_logs:
     neo4j_import:
     neo4j_plugins:
   
   networks:
     graph-rag-network:
       driver: bridge
   ```

3. **Deploy with Docker Compose**
   ```bash
   # Set environment variables
   export NEO4J_PASSWORD=your_secure_password
   
   # Deploy
   docker-compose -f docker-compose.prod.yml up -d
   ```

### Kubernetes Deployment

1. **Create Namespace**
   ```yaml
   # namespace.yaml
   apiVersion: v1
   kind: Namespace
   metadata:
     name: graph-rag
   ```

2. **Create ConfigMap**
   ```yaml
   # configmap.yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
     name: graph-rag-config
     namespace: graph-rag
   data:
     application.yml: |
       spring:
         profiles: prod
         neo4j:
           uri: bolt://graph-rag-neo4j:7687
           authentication:
             username: neo4j
             password: ${NEO4J_PASSWORD}
       logging:
         level:
           com.vividcodes.graphrag: INFO
       server:
         port: 8080
   ```

3. **Create Secret**
   ```yaml
   # secret.yaml
   apiVersion: v1
   kind: Secret
   metadata:
     name: graph-rag-secret
     namespace: graph-rag
   type: Opaque
   data:
     neo4j-password: <base64-encoded-password>
   ```

4. **Create Deployment**
   ```yaml
   # deployment.yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: graph-rag-app
     namespace: graph-rag
   spec:
     replicas: 3
     selector:
       matchLabels:
         app: graph-rag
     template:
       metadata:
         labels:
           app: graph-rag
       spec:
         containers:
         - name: graph-rag
           image: graph-rag:latest
           ports:
           - containerPort: 8080
           env:
           - name: SPRING_PROFILES_ACTIVE
             value: "prod"
           - name: NEO4J_PASSWORD
             valueFrom:
               secretKeyRef:
                 name: graph-rag-secret
                 key: neo4j-password
           volumeMounts:
           - name: config
             mountPath: /app/config
           livenessProbe:
             httpGet:
               path: /api/v1/health
               port: 8080
             initialDelaySeconds: 60
             periodSeconds: 30
           readinessProbe:
             httpGet:
               path: /api/v1/health
               port: 8080
             initialDelaySeconds: 30
             periodSeconds: 10
         volumes:
         - name: config
           configMap:
             name: graph-rag-config
   ```

5. **Create Service**
   ```yaml
   # service.yaml
   apiVersion: v1
   kind: Service
   metadata:
     name: graph-rag-service
     namespace: graph-rag
   spec:
     selector:
       app: graph-rag
     ports:
     - protocol: TCP
       port: 80
       targetPort: 8080
     type: LoadBalancer
   ```

6. **Deploy to Kubernetes**
   ```bash
   kubectl apply -f namespace.yaml
   kubectl apply -f configmap.yaml
   kubectl apply -f secret.yaml
   kubectl apply -f deployment.yaml
   kubectl apply -f service.yaml
   ```

### Cloud Deployment

#### AWS Deployment

1. **EC2 Deployment**
   ```bash
   # Launch EC2 instance
   aws ec2 run-instances \
     --image-id ami-0c02fb55956c7d316 \
     --instance-type t3.medium \
     --key-name your-key-pair \
     --security-group-ids sg-xxxxxxxxx
   
   # Install dependencies
   sudo yum update -y
   sudo yum install -y java-17-openjdk maven docker
   sudo systemctl start docker
   sudo usermod -a -G docker ec2-user
   
   # Deploy application
   git clone <repository-url>
   cd vividcodes-graph-rag
   docker-compose up -d
   ```

2. **ECS Deployment**
   ```bash
   # Create ECS cluster
   aws ecs create-cluster --cluster-name graph-rag-cluster
   
   # Create task definition
   aws ecs register-task-definition --cli-input-json file://task-definition.json
   
   # Create service
   aws ecs create-service \
     --cluster graph-rag-cluster \
     --service-name graph-rag-service \
     --task-definition graph-rag:1 \
     --desired-count 2
   ```

#### Google Cloud Platform

1. **GKE Deployment**
   ```bash
   # Create GKE cluster
   gcloud container clusters create graph-rag-cluster \
     --zone us-central1-a \
     --num-nodes 3
   
   # Deploy application
   kubectl apply -f k8s/
   ```

#### Azure

1. **AKS Deployment**
   ```bash
   # Create AKS cluster
   az aks create \
     --resource-group graph-rag-rg \
     --name graph-rag-cluster \
     --node-count 3
   
   # Deploy application
   kubectl apply -f k8s/
   ```

## Configuration Management

### Environment-Specific Configuration

**application-prod.yml**
```yaml
spring:
  profiles: prod
  neo4j:
    uri: bolt://neo4j:7687
    authentication:
      username: neo4j
      password: ${NEO4J_PASSWORD}
    connection-timeout: 30s
    connection-pool:
      max-connection-lifetime: 1h
      max-connection-pool-size: 100

logging:
  level:
    com.vividcodes.graphrag: INFO
    org.neo4j: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

server:
  port: 8080
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

### External Configuration

Use environment variables for sensitive configuration:

```bash
export NEO4J_PASSWORD=your_secure_password
export SPRING_PROFILES_ACTIVE=prod
export JAVA_OPTS="-Xmx2g -Xms1g"
```

## Monitoring and Logging

### Application Monitoring

1. **Health Checks**
   ```bash
   # Application health
   curl http://localhost:8080/api/v1/health
   
   # Actuator endpoints
   curl http://localhost:8080/actuator/health
   curl http://localhost:8080/actuator/metrics
   ```

2. **Logging Configuration**
   ```yaml
   logging:
     file:
       name: logs/graph-rag.log
     pattern:
       file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
     level:
       com.vividcodes.graphrag: INFO
       org.neo4j: WARN
       org.springframework: INFO
   ```

3. **Metrics Collection**
   ```yaml
   management:
     metrics:
       export:
         prometheus:
           enabled: true
     endpoint:
       metrics:
         enabled: true
   ```

### Database Monitoring

1. **Neo4j Monitoring**
   ```cypher
   // Check database status
   CALL dbms.components() YIELD name, versions, edition
   
   // Check active connections
   CALL dbms.listConnections() YIELD connector, connectTime, connectorAddress
   
   // Check memory usage
   CALL dbms.queryJmx("java.lang:type=Memory") YIELD attributes
   ```

## Security Configuration

### Network Security

1. **Firewall Rules**
   ```bash
   # Allow only necessary ports
   sudo ufw allow 8080/tcp  # Application
   sudo ufw allow 7474/tcp  # Neo4j HTTP
   sudo ufw allow 7687/tcp  # Neo4j Bolt
   ```

2. **SSL/TLS Configuration**
   ```yaml
   server:
     ssl:
       enabled: true
       key-store: classpath:keystore.p12
       key-store-password: ${SSL_KEYSTORE_PASSWORD}
       key-store-type: PKCS12
   ```

### Authentication & Authorization

1. **API Authentication**
   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://your-auth-server/.well-known/openid_configuration
   ```

2. **Database Security**
   ```yaml
   spring:
     neo4j:
       authentication:
         username: neo4j
         password: ${NEO4J_PASSWORD}
       ssl:
         enabled: true
   ```

## Backup and Recovery

### Database Backup

1. **Neo4j Backup**
   ```bash
   # Create backup
   neo4j-admin backup --database=neo4j --backup-dir=/backups
   
   # Restore backup
   neo4j-admin restore --database=neo4j --from=/backups/neo4j-2025-07-25
   ```

2. **Automated Backup Script**
   ```bash
   #!/bin/bash
   BACKUP_DIR="/backups/neo4j"
   DATE=$(date +%Y-%m-%d_%H-%M-%S)
   
   # Create backup
   docker exec graph-rag-neo4j neo4j-admin backup \
     --database=neo4j \
     --backup-dir=/backups \
     --name=neo4j-$DATE
   
   # Compress backup
   tar -czf $BACKUP_DIR/neo4j-$DATE.tar.gz $BACKUP_DIR/neo4j-$DATE
   
   # Clean old backups (keep last 7 days)
   find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete
   ```

## Troubleshooting

### Common Issues

1. **Application Won't Start**
   ```bash
   # Check logs
   docker logs graph-rag-app
   
   # Check port availability
   netstat -tulpn | grep 8080
   
   # Check Java version
   java -version
   ```

2. **Database Connection Issues**
   ```bash
   # Check Neo4j status
   docker logs graph-rag-neo4j
   
   # Test connection
   curl http://localhost:7474/browser/
   
   # Check network connectivity
   docker exec graph-rag-app ping neo4j
   ```

3. **Memory Issues**
   ```bash
   # Check memory usage
   docker stats
   
   # Increase heap size
   export JAVA_OPTS="-Xmx4g -Xms2g"
   ```

### Performance Tuning

1. **JVM Tuning**
   ```bash
   export JAVA_OPTS="
     -Xmx4g -Xms2g
     -XX:+UseG1GC
     -XX:MaxGCPauseMillis=200
     -XX:+UseStringDeduplication
   "
   ```

2. **Neo4j Tuning**
   ```yaml
   # neo4j.conf
   dbms.memory.heap.initial_size=2G
   dbms.memory.heap.max_size=4G
   dbms.memory.pagecache.size=2G
   dbms.connector.bolt.thread_pool_min_size=5
   dbms.connector.bolt.thread_pool_max_size=400
   ```

## Scaling

### Horizontal Scaling

1. **Load Balancer Configuration**
   ```nginx
   # nginx.conf
   upstream graph_rag_backend {
       server app1:8080;
       server app2:8080;
       server app3:8080;
   }
   
   server {
       listen 80;
       location / {
           proxy_pass http://graph_rag_backend;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

2. **Auto Scaling**
   ```yaml
   # Kubernetes HPA
   apiVersion: autoscaling/v2
   kind: HorizontalPodAutoscaler
   metadata:
     name: graph-rag-hpa
   spec:
     scaleTargetRef:
       apiVersion: apps/v1
       kind: Deployment
       name: graph-rag-app
     minReplicas: 2
     maxReplicas: 10
     metrics:
     - type: Resource
       resource:
         name: cpu
         target:
           type: Utilization
           averageUtilization: 70
   ```

## Disaster Recovery

### Backup Strategy

1. **Database Backups**: Daily automated backups
2. **Application Backups**: Configuration and deployment scripts
3. **Documentation**: Architecture and operational procedures

### Recovery Procedures

1. **Database Recovery**: Restore from latest backup
2. **Application Recovery**: Redeploy from source control
3. **Data Validation**: Verify system integrity after recovery

## Maintenance

### Regular Maintenance Tasks

1. **Database Maintenance**
   ```cypher
   // Optimize database
   CALL db.optimize();
   
   // Update statistics
   CALL db.stats.clear();
   CALL db.stats.collect();
   ```

2. **Application Updates**
   ```bash
   # Update application
   git pull origin main
   mvn clean package
   docker-compose down
   docker-compose up -d
   ```

3. **Security Updates**
   ```bash
   # Update base images
   docker pull neo4j:5.15
   docker pull openjdk:17-jre
   ```

### Monitoring Alerts

1. **Application Alerts**
   - High CPU usage (>80%)
   - High memory usage (>90%)
   - Application errors (>5% error rate)

2. **Database Alerts**
   - Connection pool exhaustion
   - Slow query performance
   - Disk space usage (>85%)

3. **Infrastructure Alerts**
   - Service unavailability
   - Network connectivity issues
   - Resource exhaustion 