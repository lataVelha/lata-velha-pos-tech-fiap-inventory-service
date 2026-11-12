# Provider AWS usado pelo backend S3/remote state e pelos recursos de ECR.
# A borda (API Gateway/ALB) agora vive no repo infra (addons + edge); o
# servico so mantem o Deployment/Service/etc via kubectl.
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
