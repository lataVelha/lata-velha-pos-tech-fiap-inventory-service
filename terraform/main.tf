# O inventory-service usa MongoDB, provisionado no proprio Kubernetes
# (k8s/mongodb-deployment.yaml + mongodb-service.yaml). Nao ha mais RDS nem
# dependencia do state do infra-db.

# Barramento SNS/SQS (Epic 13) — topic ARN e a fila deste serviço.
data "terraform_remote_state" "messaging" {
  backend = "s3"
  config = {
    bucket = var.state_bucket
    key    = var.messaging_state_key
    region = var.region
  }
}

locals {
  messaging = data.terraform_remote_state.messaging.outputs
}

module "app" {
  source = "./modules/app"

  docker_image  = var.docker_image
  node_port     = var.node_port
  mail_username = var.mail_username
  mail_password = var.mail_password
  dd_api_key    = var.dd_api_key

  messaging_topic_arn = local.messaging.sns_topic_arn
  messaging_queue_url = local.messaging.inventory_queue_url
  service_version     = var.service_version
}

# --------------------------------------------------------------------------
# ECR proprio do servico. A imagem continua chegando por TF_VAR_docker_image;
# o pipeline (Stories 10.3/10.4) le este output para fazer o build/push no
# repositorio correto, sem reutilizar o ECR do `app`.
# --------------------------------------------------------------------------
resource "aws_ecr_repository" "inventory_service" {
  name                 = "lata-velha-inventory-service"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }
}

resource "aws_ecr_lifecycle_policy" "inventory_service" {
  repository = aws_ecr_repository.inventory_service.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Manter so as 10 imagens mais recentes"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = { type = "expire" }
      }
    ]
  })
}


# As rotas do API Gateway e as listener rules do ALB ficam centralizadas no
# repo infra (addons + edge). Este stack so provisiona o cluster/deploy do
# servico e o ECR proprio.
