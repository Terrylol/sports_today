# Linux Server Deployment Guide

This guide describes how to deploy the Team History project (Java Spring Boot + Vue.js) on a Linux server (Ubuntu/CentOS).

## 1. Prerequisites

You need to install the following software on your server:

- **Java JDK 17+**: For running the backend.
- **Node.js 18+ & npm**: For building the frontend.
- **Nginx**: For serving the frontend and reverse proxying API requests.
- **Git**: For cloning the repository.
- **Maven**: For building the backend (optional if you upload the jar directly, but recommended).

### Installation Examples (Ubuntu)

```bash
# Update packages
sudo apt update

# Install OpenJDK 17
sudo apt install openjdk-17-jdk -y

# Install Node.js (using NodeSource)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Install Nginx
sudo apt install nginx -y

# Install Git and Maven
sudo apt install git maven -y
```

## 2. Clone the Repository

Clone the code to your server (e.g., in `/var/www` or your home directory).

```bash
cd /var/www
sudo git clone https://github.com/Terrylol/sports_today.git team-history
cd team-history
```

## 3. Backend Deployment

1.  **Build the application**

    ```bash
    cd backend
    mvn clean package -DskipTests
    ```

    This will generate a jar file at `target/team-history-0.0.1-SNAPSHOT.jar`.

2.  **Run the application**

    You can run it directly for testing:

    ```bash
    java -jar target/team-history-0.0.1-SNAPSHOT.jar
    ```

    **Recommended: Use Systemd for background service**

    Create a service file: `sudo nano /etc/systemd/system/team-history.service`

    ```ini
    [Unit]
    Description=Team History Backend
    After=syslog.target

    [Service]
    User=root
    # Change path to your actual path
    ExecStart=/usr/bin/java -jar /var/www/team-history/backend/target/team-history-0.0.1-SNAPSHOT.jar
    SuccessExitStatus=143

    [Install]
    WantedBy=multi-user.target
    ```

    Start and enable the service:

    ```bash
    sudo systemctl daemon-reload
    sudo systemctl start team-history
    sudo systemctl enable team-history
    ```

    The backend will run on port **8081** (defined in `application.properties`).

## 4. Frontend Deployment

1.  **Install dependencies and build**

    ```bash
    cd ../frontend
    npm install
    npm run build
    ```

    The build artifacts will be in the `dist` directory.

2.  **Configure Nginx**

    Edit the Nginx configuration: `sudo nano /etc/nginx/sites-available/default` (or create a new file).

    Replace the content with:

    ```nginx
    server {
        listen 80;
        server_name your_domain_or_ip;

        # Serve Frontend Static Files
        location / {
            # Change path to your actual frontend/dist path
            root /var/www/team-history/frontend/dist;
            index index.html;
            try_files $uri $uri/ /index.html;
        }

        # Proxy API Requests to Backend
        location /api {
            proxy_pass http://localhost:8081;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
    }
    ```

3.  **Restart Nginx**

    ```bash
    sudo nginx -t
    sudo systemctl restart nginx
    ```

## 5. Verification

- Open your browser and visit `http://your_server_ip`.
- You should see the Vue.js application.
- API requests will be handled by Nginx and forwarded to the Java backend.

## 6. Security Note

- The `application.properties` currently uses an in-memory H2 database. Data will be lost on restart. For production, consider using MySQL/PostgreSQL.
- The Admin password and API Key are in the properties file. Consider using environment variables for production security.
