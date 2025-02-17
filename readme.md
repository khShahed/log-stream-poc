## Build Docker Image
```bash
docker build -t log-stream-poc-service .
```

## Kubernetes Deployment
### Create Namespace:
```bash
kubectl create namespace dev-log-stream-poc
```
### Apply the manifests:
```bash
kubectl apply -f k8s/ -n dev-log-stream-poc
```
