# Necessario desde que este repo passou a criar recursos aws_apigatewayv2_*
# diretamente (integracao com o ALB + rotas publicas/protegidas) — antes só
# tinha o provider kubectl, porque o unico recurso daqui era o modulo "app"
# (Deployment/Service/etc, tudo via kubectl).
provider "aws" {
  region = var.region
}

provider "kubectl" {
  host                   = var.cluster_endpoint
  cluster_ca_certificate = base64decode(var.cluster_ca_data)
  load_config_file       = false

  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", var.cluster_name, "--region", var.region]
  }
}
