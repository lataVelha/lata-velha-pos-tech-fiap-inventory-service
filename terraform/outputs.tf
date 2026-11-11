output "app_api_endpoint" {
  description = "URL pública da aplicação (API Gateway)"
  value       = local.addons.app_api_endpoint
}

output "ecr_repository_url" {
  description = "URL do repositorio ECR exclusivo do inventory-service"
  value       = aws_ecr_repository.inventory_service.repository_url
}
