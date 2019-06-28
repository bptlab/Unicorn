# Narwhal: An Unicorn integration based on Docker
## Install and usage Narwhal on your machine
1. Install Docker and Docker-compose as its described in official Docker Docs.
2. Clone the Unicorn repository from GitHub to your local machine:
    ```sh
    git clone https://github.com/bptlab/Unicorn.git
    ```
3. Go to _Path_To_UnicornFolder/narwhal_ and copy the file _envtemplate.txt_.
4. Adapt the copy of the file _envtemplate.txt_ as follow: 
    1. Replace "yourdbpasswd" by a password which will set as password for the root user of the MySQL-Database
    2. Replace "tomcatadmin" by an username which will be used to administrate the Tomcat server.
    3. Replace "yourtomcatadmin" by a password which will be set for the user created in step 4.2.
    4. Last but not least, replace "pathToYourRepository" by the path where the cloned repository exists on your hard drive.
    * NOTE: Step one to three are optional. If you do not replace the standard configuration, Narwhal will generate new ones because of security issues. 
5. Rename the edited copy of the file _envtemplate.txt_ to _.env_. Yes, you are rigth, it's only the file type suffix without any file name!
6. Now you are ready to start Narwhal by executing inside the dircetory _Path_To_UnicornFolder/narwhal_ following command:
    ```sh
    docker-compose up
    ```
* Now the Docker-Compose Deamon build everything. If you start it the first time, the process will take some time. The output _"narwhal-tomcatsrv | INFO: Server startup in x ms"_ informes about the process finished. 
7. To use Unicorn, open your browser and go to _http://localhost:8888/Unicorn/_
8. You can stop Narwhal by executing inside the directory _Path_To_UnicornFolder/narwhal_ the following command:
    ```sh
    docker-compose down
    ```