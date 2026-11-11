variable "docker_image" {
  description = "Imagem ECR da aplicacao"
  type        = string
}

variable "node_port" {
  description = "NodePort exposto pelo Service do servico"
  type        = number
}

variable "messaging_topic_arn" {
  description = "ARN do topico SNS de eventos de dominio"
  type        = string
}

variable "messaging_queue_url" {
  description = "URL da fila SQS consumida por este servico"
  type        = string
}

variable "service_version" {
  description = "SHA/tag da imagem — vira a tag service.version nos spans"
  type        = string
}

variable "db_url" {
  description = "JDBC URL do banco de dados"
  type        = string
}

variable "db_username" {
  description = "Usuario do banco de dados"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "Senha do banco de dados"
  type        = string
  sensitive   = true
}

variable "mail_username" {
  description = "Email remetente (Gmail)"
  type        = string
  sensitive   = true
}

variable "mail_password" {
  description = "Senha de app do Gmail"
  type        = string
  sensitive   = true
}

variable "dd_api_key" {
  description = "Datadog API key para OTLP ingest"
  type        = string
  sensitive   = true
}
