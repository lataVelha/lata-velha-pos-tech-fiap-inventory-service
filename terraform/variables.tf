variable "region" {
  description = "Regiao da AWS"
  type        = string
  default     = "us-east-1"
}

variable "state_bucket" {
  description = "Bucket S3 onde este state e gravado e onde o infra-db publica o dele"
  type        = string
}

variable "messaging_state_key" {
  description = "Chave do state do infra messaging, de onde vêm o topic ARN e a fila deste serviço"
  type        = string
  default     = "lata-velha/infra-messaging/terraform.tfstate"
}

# Conexao com o EKS (provisionado pelo repo infra) — lidas via AWS CLI no
# pipeline/apply.sh e passadas como TF_VAR_, ja que provider config nao usa
# data sources para a propria inicializacao.
variable "cluster_endpoint" {
  description = "Endpoint do EKS cluster"
  type        = string
}

variable "cluster_ca_data" {
  description = "CA certificate do EKS cluster (base64)"
  type        = string
  sensitive   = true
}

variable "cluster_name" {
  description = "Nome do EKS cluster"
  type        = string
}

variable "node_port" {
  description = "NodePort do Service do inventory-service (app usa 30080, os 30082, billing 30081)"
  type        = number
  default     = 30083
}

variable "docker_image" {
  description = "Imagem ECR da aplicacao (definida pelo pipeline apos o build/push)"
  type        = string
}

variable "mail_username" {
  description = "Email remetente (Gmail) — obrigatorio, nao pode ser vazio"
  type        = string
  sensitive   = true

  validation {
    condition     = length(trimspace(var.mail_username)) > 0
    error_message = "mail_username nao pode ser vazio. Sem mail configurado o pod entra em CrashLoopBackOff (o startupProbe bate em /actuator/health, que falha se o SMTP nao autentica)."
  }
}

variable "mail_password" {
  description = "Senha de app do Gmail — obrigatoria, nao pode ser vazia"
  type        = string
  sensitive   = true

  validation {
    condition     = length(trimspace(var.mail_password)) > 0
    error_message = "mail_password nao pode ser vazio. Mesmo motivo do mail_username."
  }
}

variable "dd_api_key" {
  description = "Datadog API key para OTLP ingest"
  type        = string
  sensitive   = true
  default     = ""
}

variable "service_version" {
  description = "SHA/tag da imagem — vira a tag service.version nos spans"
  type        = string
  default     = "0.0.1"
}
