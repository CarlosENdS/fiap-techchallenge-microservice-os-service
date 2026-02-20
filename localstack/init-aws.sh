#!/bin/bash
# LocalStack SQS Queue Initialization Script
# This script creates all required SQS queues for local development and testing

echo "Initializing AWS SQS queues in LocalStack..."

# Wait for LocalStack to be ready
sleep 5

# Create FIFO queue for outbound OS events
awslocal sqs create-queue \
  --queue-name os-order-events-queue.fifo \
  --attributes FifoQueue=true,ContentBasedDeduplication=false

# Create standard queue for billing service (OS -> Billing)
awslocal sqs create-queue --queue-name service-order-events

# Create standard queues for inbound events (Saga pattern)
awslocal sqs create-queue --queue-name quote-approved-queue
awslocal sqs create-queue --queue-name execution-completed-queue
awslocal sqs create-queue --queue-name payment-failed-queue
awslocal sqs create-queue --queue-name resource-unavailable-queue

echo "SQS queues created successfully!"

# List all queues
echo "Available queues:"
awslocal sqs list-queues
