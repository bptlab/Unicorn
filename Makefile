DB_USER_NAME="root"
DB_PASSWORD="SecretPassword"
SCHEMA_NAME="eap_development"
PROJECT="unicorn"
IMAGE_TAG="latest"
PORT=8181

release_local: clean build deploy_local

build:
	mvn install -DskipTests

clean:
	mvn clean

deploy_local:
	sudo cp unicorn.properties /var/lib/tomcat7/conf/
	sudo cp EapWebInterface/target/Unicorn.war /var/lib/tomcat7/webapps/

start_database:
	docker run -p 3306:3306 --name bptlab-database -e MYSQL_USER=$(DB_USER_NAME) -e MYSQL_PASSWORD=$(DB_PASSWORD) -e MYSQL_DATABASE=$(SCHEMA_NAME) -e MYSQL_ROOT_PASSWORD=$(DB_PASSWORD) -d mysql:5.7

docker: build_docker

build_docker:
	make build
	docker build . -t bptlab/$(PROJECT):$(IMAGE_TAG)

run_docker:
	docker-compose up
