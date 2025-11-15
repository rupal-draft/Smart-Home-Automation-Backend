#!/bin/bash

set -e

echo "=== Creating KIND config ==="
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

echo "=== Installing ingress-nginx for KIND ==="
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

echo "=== Applying your Kubernetes manifests ==="
cd /mnt/c/Users/rupal/Documents/smart-home-automation-system/k8s
kubectl apply -R -f .

echo "=== Health Check ==="
curl -s http://localhost/api/v1/actuator/health || true

echo "=== DONE ==="
