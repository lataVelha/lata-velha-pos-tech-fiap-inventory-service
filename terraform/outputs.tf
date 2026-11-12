output "ecr_repository_url" {
  description = "URL do repositorio ECR exclusivo do inventory-service"
  value       = aws_ecr_repository.inventory_service.repository_url
}
