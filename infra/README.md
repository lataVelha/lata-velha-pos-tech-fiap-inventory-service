# Infraestrutura do inventory-service

Este diretório era uma cópia da documentação de infraestrutura do monolito
(`app`), que foi removido junto com o repo `infra-db`.

A infraestrutura real do inventory-service está em:

- `terraform/` — ECR próprio e Deployment/Service/ConfigMap/Secret/HPA/PDB
  no namespace `lata-velha-inventory`. O banco é **MongoDB in-cluster**
  (`k8s/mongodb-deployment.yaml` + `k8s/mongodb-service.yaml`), sem RDS.
- `k8s/` — manifests renderizados pelo Terraform.
- `.github/workflows/main.yml` — pipeline de CI/CD do serviço.

NodePort: **30083**. State: `lata-velha/inventory-service/terraform.tfstate`.
