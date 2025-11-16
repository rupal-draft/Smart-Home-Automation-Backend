#!/bin/bash

set -e

echo "==================================="
echo "       Creating KIND Cluster"
echo "==================================="

cat > kind-config.yaml <<EOF
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  extraPortMappings:
    - containerPort: 80
      hostPort: 80
      protocol: TCP
    - containerPort: 443
      hostPort: 443
      protocol: TCP
  labels:
    ingress-ready: "true"
- role: worker
EOF

echo "=== Creating KIND cluster ==="
kind create cluster --name shas --config kind-config.yaml

echo "=== Checking nodes ==="
kubectl get nodes

echo "=== Labeling control-plane node ==="
kubectl label node shas-control-plane ingress-ready=true --overwrite

echo "==================================="
echo " Installing ingress-nginx for KIND"
echo "==================================="

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.1/deploy/static/provider/kind/deploy.yaml

echo "=== Waiting for ingress-nginx to be Ready ==="
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=180s

echo "==================================="
echo "Applying Your App Kubernetes Manifests"
echo "==================================="

cd /mnt/c/Users/rupal/Documents/smart-home-automation-system/k8s
kubectl apply -R -f .

echo "=== API Health Check ==="
curl -s http://localhost/api/v1/actuator/health || true


###############################################
#                ARGOCD SETUP                 #
###############################################

echo "==================================="
echo "          Installing ArgoCD"
echo "==================================="

kubectl create namespace argocd || true

kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

echo "=== Waiting for ArgoCD components to be Ready ==="
kubectl wait --for=condition=ready pod -n argocd --all --timeout=300s

echo "==================================="
echo "  Exposing ArgoCD Server (NodePort)"
echo "==================================="

kubectl patch svc argocd-server -n argocd \
  -p '{"spec": {"type": "NodePort"}}'

echo "=== Fetching ArgoCD NodePort ==="
ARGO_PORT=$(kubectl get svc argocd-server -n argocd -o=jsonpath='{.spec.ports[1].nodePort}')

echo "ArgoCD UI will be available at:"
echo "👉 https://localhost:${ARGO_PORT}"

echo "==================================="
echo "      Fetching ArgoCD Password"
echo "==================================="

ADMIN_PASS=$(kubectl -n argocd get secret argocd-initial-admin-secret \
  -o jsonpath="{.data.password}" | base64 --decode)

echo "ArgoCD Admin User: admin"
echo "ArgoCD Admin Password: $ADMIN_PASS"

echo "==================================="
echo "You can log in to ArgoCD now!"
echo "==================================="

echo "=== Optional: Auto port-forwarding to 8080 ==="
echo "Run manually if needed:"
echo "kubectl port-forward svc/argocd-server -n argocd 8080:443"

echo "==================================="
echo "                 DONE"
echo "==================================="
