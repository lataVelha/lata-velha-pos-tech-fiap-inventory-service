# O banco de dados (RDS) e provisionado pelo repo infra-db, que publica seu
# state neste mesmo bucket. Precisa rodar antes deste deploy.
data "terraform_remote_state" "infra_db" {
  backend = "s3"
  config = {
    bucket = var.state_bucket
    key    = "lata-velha/infra-db/terraform.tfstate"
    region = var.region
  }
}

# "Casco" do API Gateway (API + VPC Link + Stage, sem rotas) e o ALB —
# ambos criados pelo repo infra (addons). Este repo anexa a propria
# integracao com o ALB + as rotas publicas/protegidas.
data "terraform_remote_state" "addons" {
  backend = "s3"
  config = {
    bucket = var.state_bucket
    key    = var.addons_state_key
    region = var.region
  }
}

# jwt_authorizer_id — anexado pelo repo lambda no mesmo API Gateway. As
# rotas protegidas abaixo referenciam essa authorizer.
data "terraform_remote_state" "lambda" {
  backend = "s3"
  config = {
    bucket = var.state_bucket
    key    = var.lambda_state_key
    region = var.region
  }
}

locals {
  infra_db = data.terraform_remote_state.infra_db.outputs
  addons   = data.terraform_remote_state.addons.outputs
  lambda   = data.terraform_remote_state.lambda.outputs

  # Rotas PUBLICAS (sem authorizer) — espelham exatamente os .permitAll() do
  # SecurityConfig.java do app.
  public_routes = [
    "ANY /actuator/health",
    "ANY /actuator/health/{proxy+}",
    "GET /swagger-ui.html",
    "ANY /swagger-ui/{proxy+}",
    "GET /v3/api-docs",
    "ANY /v3/api-docs/{proxy+}",
    "GET /v3/api-docs.yaml",
    "ANY /auth/{proxy+}",
    "POST /ordens-servico/{id}/aprovacao-orcamento",
  ]
}

module "app" {
  source = "./modules/app"

  docker_image  = var.docker_image
  db_url        = "jdbc:postgresql://${local.infra_db.rds_endpoint}/${local.infra_db.db_name}"
  db_username   = local.infra_db.db_username
  db_password   = local.infra_db.db_password
  mail_username = var.mail_username
  mail_password = var.mail_password
  dd_api_key    = var.dd_api_key
}

# --------------------------------------------------------------------------
# Anexacao no API Gateway compartilhado (repo infra, addons): integracao
# privada HTTP_PROXY com o ALB interno + rotas publicas/protegidas. O
# "casco" do gateway (API + VPC Link + Stage) ja vem pronto do repo infra;
# a lambda authorizer usada nas rotas protegidas ja vem anexada pelo repo
# lambda (jwt_authorizer_id, lido acima via remote_state).
# --------------------------------------------------------------------------

# payload_format_version precisa ser "1.0" — integracoes privadas (VPC_LINK)
# nao suportam 2.0.
resource "aws_apigatewayv2_integration" "alb" {
  api_id                 = local.addons.app_api_id
  integration_type       = "HTTP_PROXY"
  integration_uri        = local.addons.alb_listener_arn
  integration_method     = "ANY"
  connection_type        = "VPC_LINK"
  connection_id          = local.addons.vpc_link_id
  payload_format_version = "1.0"
}

resource "aws_apigatewayv2_route" "public" {
  for_each  = toset(local.public_routes)
  api_id    = local.addons.app_api_id
  route_key = each.value
  target    = "integrations/${aws_apigatewayv2_integration.alb.id}"
}

# Rotas PROTEGIDAS — tudo que nao esta na lista publica acima, exigindo a
# authorizer (repo lambda).
resource "aws_apigatewayv2_route" "protected_root" {
  api_id             = local.addons.app_api_id
  route_key          = "ANY /"
  target             = "integrations/${aws_apigatewayv2_integration.alb.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = local.lambda.jwt_authorizer_id
}

resource "aws_apigatewayv2_route" "protected_proxy" {
  api_id             = local.addons.app_api_id
  route_key          = "ANY /{proxy+}"
  target             = "integrations/${aws_apigatewayv2_integration.alb.id}"
  authorization_type = "CUSTOM"
  authorizer_id      = local.lambda.jwt_authorizer_id
}
