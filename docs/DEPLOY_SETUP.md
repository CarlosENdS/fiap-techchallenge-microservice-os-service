# Configuração do CD Pipeline e IRSA

Este documento descreve a configuração necessária para deploy do os-service.

## 📋 Visão Geral

| Componente | Método de Autenticação |
|------------|------------------------|
| Pipeline CD (GitHub Actions) | Credenciais estáticas (IAM User) |
| Pod (os-service) | IRSA (token OIDC automático) |

## 🔧 Configuração Manual Necessária

### GitHub Secrets

Configure os seguintes **secrets** no repositório GitHub:
**Settings → Secrets and variables → Actions → New repository secret**

| Secret Name | Descrição | Onde Obter |
|-------------|-----------|------------|
| `AWS_ACCESS_KEY_ID` | Access Key do IAM User | Console AWS ou terraform output |
| `AWS_SECRET_ACCESS_KEY` | Secret Access Key | Console AWS ou terraform output |
| `DB_PASSWORD` | Senha do banco RDS (os_service_user) | Definida no `rds.tf` (OsService2024!) |
| `TF_API_TOKEN` | Token de API do Terraform Cloud | app.terraform.io → User Settings → Tokens |

### Terraform Cloud

1. Crie uma conta em [app.terraform.io](https://app.terraform.io)
2. Crie uma organização: `fiap-techchallenge`
3. Crie um workspace: `cargarage-infra-database`
4. Gere um API token e configure como `TF_API_TOKEN` no GitHub

### Variáveis de Ambiente no cd.yml

Ajuste conforme sua configuração:

```yaml
env:
  AWS_REGION: us-east-1
  ECR_REPOSITORY: cargarage-os-service
  EKS_CLUSTER_NAME: cargarage-cluster
  TF_CLOUD_ORGANIZATION: fiap-techchallenge
  TF_WORKSPACE: cargarage-infra-database
```

## 🔄 Fluxo do Deploy

```
┌─────────────────────────────────────────────────────────────────────────┐
│  1. TERRAFORM (infra-database)                                          │
│     - Provisiona RDS (cargarage + os_service_db), SQS, IAM Roles        │
│     - Outputs salvos no Terraform Cloud                                 │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  2. CD PIPELINE (GitHub Actions)                                        │
│     - Autentica com credenciais estáticas (IAM User)                    │
│     - Busca outputs do Terraform Cloud via tfc-workflows-github action  │
│     - Substitui placeholders nos manifests K8s (sed)                    │
│     - kubectl apply dos recursos                                         │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│  3. POD (EKS)                                                           │
│     - ServiceAccount anotado com IRSA role ARN                          │
│     - Token OIDC montado automaticamente pelo EKS                       │
│     - SDK AWS assume a role e acessa SQS sem credenciais                │
└─────────────────────────────────────────────────────────────────────────┘
```

## 🗄️ Estrutura do RDS

O RDS PostgreSQL contém dois bancos de dados:

| Database | User | Aplicação |
|----------|------|-----------|
| `cargarage` | `postgres` | Monolito Car Garage |
| `os_service_db` | `os_service_user` | Microservice OS Service |

### Inicialização dos Bancos

A criação e população dos bancos é feita **automaticamente via Terraform** durante o `terraform apply`. 

O Terraform executa 3 Kubernetes Jobs em sequência:

1. **cargarage-db-seed**: Popula o banco `cargarage` (monolito)
2. **os-service-db-init**: Cria o database `os_service_db` e user `os_service_user`
3. **os-service-db-seed**: Popula o banco `os_service_db` (microservice)

```bash
# Verificar status dos jobs
kubectl get jobs -n db-init

# Ver logs
kubectl logs job/cargarage-db-seed -n db-init
kubectl logs job/os-service-db-init -n db-init
kubectl logs job/os-service-db-seed -n db-init
```

Os scripts SQL estão em `terraform/scripts/`:
- `seed-cargarage.sql`: Schema e dados do monolito
- `init-os-service-db.sql`: Cria database e user os_service
- `seed-os-service.sql`: Schema e dados do microservice

## 🔐 IRSA (IAM Roles for Service Accounts)

### O que é IRSA?

IRSA permite que pods EKS assumam IAM roles sem credenciais estáticas. O EKS injeta automaticamente um token OIDC no pod, que o AWS SDK usa para assumir a role IAM.

### Como funciona

1. **Terraform cria**: IAM Role com trust policy para o OIDC Provider do EKS
2. **ServiceAccount**: Anotado com `eks.amazonaws.com/role-arn`
3. **EKS injeta**: Token OIDC montado em `/var/run/secrets/eks.amazonaws.com/serviceaccount/token`
4. **AWS SDK**: Detecta automaticamente e assume a role

### Verificar se IRSA está funcionando

```bash
# Verificar ServiceAccount
kubectl get sa os-service-sa -n os-service -o yaml

# Verificar se o token está montado
kubectl exec -n os-service deployment/os-service -- ls -la /var/run/secrets/eks.amazonaws.com/serviceaccount/

# Testar acesso ao SQS
kubectl exec -n os-service deployment/os-service -- aws sqs list-queues --region us-east-1
```

## 📁 Placeholders nos Manifests K8s

O pipeline usa `sed` para substituir placeholders nos manifests:

| Arquivo | Placeholder | Valor |
|---------|-------------|-------|
| `secrets.yaml` | `__DB_URL_B64__` | URL do banco (base64) |
| `secrets.yaml` | `__DB_USERNAME_B64__` | Username (base64) |
| `secrets.yaml` | `__DB_PASSWORD_B64__` | Password (base64) |
| `secrets.yaml` | `__SQS_OS_EVENTS_QUEUE_URL_B64__` | URL da fila (base64) |
| `configmap.yaml` | `__SQS_*_QUEUE__` | URLs das filas Standard |
| `service-account.yaml` | `__IRSA_ROLE_ARN__` | ARN da role IRSA |
| `app-deployment.yaml` | `__IMAGE_URI__` | URI da imagem ECR |

## 🚀 Executando o Deploy

### Automático (Push para main)

```bash
git push origin main
```

### Manual (Workflow Dispatch)

1. Vá para **Actions** no GitHub
2. Selecione **CD Pipeline**
3. Clique em **Run workflow**
4. Escolha o environment (staging/production)

## ❓ Troubleshooting

### Erro: "Unable to locate credentials"

O pod não está conseguindo assumir a role IRSA. Verifique:

1. ServiceAccount está anotado corretamente
2. IAM Role trust policy inclui o OIDC provider do EKS
3. Pod está usando o ServiceAccount correto

### Erro: "Access Denied" ao SQS

A IAM policy não tem as permissões necessárias. Verifique o `iam.tf` no infra-database.

### Erro: "terraform output" falha

Verifique:
1. `TF_API_TOKEN` está configurado corretamente
2. Organização e workspace estão corretos no `cd.yml`
3. O Terraform foi executado e produziu outputs
