gcloud auth activate-service-account --key-file=deploy_key.json
#docker build --platform linux/amd64 -t gcr.io/user-microservice-436920/verification_image .
docker build -t gcr.io/user-microservice-436920/verification_image .
docker tag verification_image gcr.io/user-microservice-436920/verification_image
docker push gcr.io/user-microservice-436920/verification_image