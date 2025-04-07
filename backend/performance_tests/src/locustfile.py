from random import choice, randint

from locust import FastHttpUser, between, task


class WebsiteUser(FastHttpUser):
    wait_time = between(1, 5)
    product_ids = []

    def on_start(self):
        response = self.client.get(
            "/api/v1/products", name="/api/v1/products", params={"size": 50}
        )

        products = response.json()["content"]
        for product in products:
            self.product_ids.append(product["id"])

    @task(5)
    def view_products(self):
        page_number = randint(1, 10)
        self.client.get(
            "/api/v1/products", name="/api/v1/products", params={"page": page_number}
        )

    @task(3)
    def view_product(self):
        product_id = choice(self.product_ids)
        self.client.get(f"/api/v1/products/{product_id}", name="/api/v1/products/:id")

    @task(1)
    def view_product_comments(self):
        product_id = choice(self.product_ids)
        self.client.get(
            f"/api/v1/products/{product_id}/comments",
            name="/api/v1/products/:id/comments",
        )
