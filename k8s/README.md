# Kubernetes Deployment Guide - OS Service Microservice (AWS EKS)

## Overview

This directory contains Kubernetes manifests for deploying the OS Service microservice on **AWS EKS** with **AWS RDS PostgreSQL** and **AWS SQS**.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         AWS Cloud                                │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │    AWS EKS      │  │    AWS RDS      │  │    AWS SQS      │  │
│  │  (Kubernetes)   │  │  (PostgreSQL)   │  │   (Queues)      │  │
│  └────────┬────────┘  └────────┬────────┘  └────────┬────────┘  │
│           │                    │                    │            │
│           └────────────────────┼────────────────────┘            │
│                                │                                  │
│                        IAM + IRSA                                │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisites

- AWS EKS cluster (1.25+)
- kubectl configured for EKS
- AWS RDS PostgreSQL instance
- AWS SQS queues created
- IRSA (IAM Roles for Service Accounts) configured
- Docker image pushed to ECR

## AWS Resources Required

### SQS Queues

Create the following SQS queues:

```bash
# FIFO queue for outbound events
aws sqs create-queue --queue-name os-order-events-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false

# Standard queues for inbound events
aws sqs create-queue --queue-name quote-approved-queue
aws sqs create-queue --queue-name execution-completed-queue
aws sqs create-queue --queue-name payment-failed-queue
aws sqs create-queue --queue-name resource-unavailable-queue
```

### RDS PostgreSQL

Create an RDS PostgreSQL instance with:
- Engine: PostgreSQL 16
- Database name: `os_service_db`
- Master username: `os_service_user`

### IAM Role for IRSA

Create an IAM role with SQS permissions and trust relationship for the EKS OIDC provider.

## Deployment Order

Deploy resources in the following order:

```bash
# 1. Create namespace
kubectl apply -f namespace.yaml

# 2. Create service account (IRSA)
kubectl apply -f service-account.yaml

# 3. Update secrets.yaml with your RDS endpoint and SQS URLs
# Then apply secrets and configmaps
kubectl apply -f secrets.yaml
kubectl apply -f configmap.yaml

# 4. Deploy application
kubectl apply -f app-deployment.yaml
kubectl apply -f app-service.yaml

# 5. Configure HPA
kubectl apply -f hpa.yaml
```

## Quick Deploy

Deploy all resources at once:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f .
```

## Verify Deployment

```bash
# Check all pods
kubectl get pods -n os-service

# Check services
kubectl get svc -n os-service

# Check HPA
kubectl get hpa -n os-service

# View logs
kubectl logs -f deployment/os-service -n os-service
```

## Access the Service

```bash
# Get the external IP (LoadBalancer)
kubectl get svc os-service -n os-service

# Port-forward for local access
kubectl port-forward svc/os-service 8081:80 -n os-service
```

## Scaling

```bash
# Manual scaling
kubectl scale deployment/os-service --replicas=3 -n os-service

# Check HPA status
kubectl describe hpa os-service-hpa -n os-service
```

## Local Testing with PostgreSQL

For local Kubernetes testing (minikube, kind), you can deploy PostgreSQL:

```bash
kubectl apply -f postgres-pvc.yaml
kubectl apply -f postgres-deployment.yaml
```

Note: In production, use AWS RDS instead of self-managed PostgreSQL.

## Cleanup

```bash
kubectl delete namespace os-service
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DB_URL | AWS RDS PostgreSQL connection URL | jdbc:postgresql://your-rds.rds.amazonaws.com:5432/os_service_db |
| CLOUD_AWS_REGION_STATIC | AWS Region | us-east-1 |
| SQS_OS_EVENTS_QUEUE_URL | SQS queue URL for events | - |
| SERVER_PORT | Application port | 8081 |

### Secrets

Secrets are base64 encoded. To update:

```bash
echo -n "your-value" | base64
```

## Monitoring

Access Prometheus metrics at `/actuator/prometheus`:

```bash
kubectl port-forward svc/os-service 8081:80 -n os-service
curl http://localhost:8081/actuator/prometheus
```

## Troubleshooting

### Pod not starting

```bash
kubectl describe pod <pod-name> -n os-service
kubectl logs <pod-name> -n os-service --previous
```

### Database connection issues

```bash
kubectl exec -it <os-service-pod> -n os-service -- sh
nc -zv <rds-endpoint> 5432
```

### SQS connection issues

Check IAM role permissions and verify SQS queue URLs are correct.
Verify IRSA is properly configured:

```bash
kubectl describe serviceaccount os-service-sa -n os-service
```
