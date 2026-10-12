locals {
  k8s_dir = "${path.module}/../../../k8s"
}

resource "kubectl_manifest" "namespace" {
  yaml_body = file("${local.k8s_dir}/namespace.yaml")
}

resource "kubectl_manifest" "configmap" {
  yaml_body  = file("${local.k8s_dir}/configmap.yaml")
  depends_on = [kubectl_manifest.namespace]
}

resource "kubectl_manifest" "secret_db" {
  yaml_body = templatefile("${local.k8s_dir}/secret.yaml", {
    db_url_b64        = base64encode(var.db_url)
    db_username_b64   = base64encode(var.db_username)
    db_password_b64   = base64encode(var.db_password)
    mail_username_b64 = base64encode(var.mail_username)
    mail_password_b64 = base64encode(var.mail_password)
    dd_api_key_b64    = base64encode(var.dd_api_key)
  })
  sensitive_fields = ["data"]
  depends_on       = [kubectl_manifest.namespace]
}

resource "kubectl_manifest" "deployment" {
  yaml_body = templatefile("${local.k8s_dir}/deployment.yaml", {
    docker_image = var.docker_image
  })
  depends_on = [
    kubectl_manifest.configmap,
    kubectl_manifest.secret_db,
  ]
}

resource "kubectl_manifest" "service" {
  yaml_body  = file("${local.k8s_dir}/service.yaml")
  depends_on = [kubectl_manifest.deployment]
}

resource "kubectl_manifest" "hpa" {
  yaml_body  = file("${local.k8s_dir}/hpa.yaml")
  depends_on = [kubectl_manifest.deployment]
}

resource "kubectl_manifest" "pdb" {
  yaml_body  = file("${local.k8s_dir}/pdb.yaml")
  depends_on = [kubectl_manifest.deployment]
}
