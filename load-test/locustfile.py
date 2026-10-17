from locust import HttpUser, task, between


class ApiUser(HttpUser):
    wait_time = between(0.5, 2)

    @task(10)
    def health(self):
        self.client.get("/actuator/health")

    @task(5)
    def health_liveness(self):
        self.client.get("/actuator/health/liveness")

    @task(3)
    def listar_os(self):
        self.client.get("/api/v1/ordem-servico")

    @task(1)
    def login(self):
        self.client.post("/api/v1/auth/login", json={
            "email": "admin@latavelha.com",
            "password": "Admin@123",
        })
