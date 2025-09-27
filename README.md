# Real Estate Market Analysis Application

## 🚀 Running the Project (using Docker Compose)

### 1. Create `.env` file
Use the `.env_template` as a base and fill in the required values:

```env
POSTGRES_USER=your_db_username
POSTGRES_PASSWORD=your_db_password
POSTGRES_DB=your_db_name

JWT_KEY=your_jwt_secret_key
JWT_EXPIRATION=your_jwt_expiration_time (in seconds)
```

#### Generate a secure JWT_KEY
Run the following command in **PowerShell** and paste the result into your `.env` file:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### 2. Run with Docker Compose (on Windows)

#### ▶️ run entire app on docker
```bash
runAllDocker.bat
```

#### ▶️ run for local dev (only needed containers on docker)
```bash
runLocalDev.bat
```
and then turn on the applications you need.

##### scoring model
In the ModelScoringowy folder: 
```bash
uvicorn server:app --reload
```
Swagger is available here: http://127.0.0.1:8000/docs

## 🗄️ Database Access via pgAdmin

After running the Docker Compose setup, you can access the PostgreSQL database through pgAdmin:

### 1. Access pgAdmin Web Interface
Open your browser and navigate to: **http://localhost:8090**

### 2. Login to pgAdmin
- **Email:** `admin@admin.com`
- **Password:** `admin`

### 3. Add PostgreSQL Server
1. Right-click on **"Servers"** in the left panel
2. Select **"Register"** → **"Server"**
3. In the **"General"** tab:
   - **Name:** `PostgreSQL Local` (or any name you prefer)
4. In the **"Connection"** tab:
   - **Host name/address:** `postgres_db`
   - **Port:** `5432`
   - **Maintenance database:** `db` (from your config.env)
   - **Username:** `user` (from your config.env)
   - **Password:** `password` (from your config.env)
5. Click **"Save"**
6. You can now access database tables:
**Databases -> db -> Schemas -> public -> Tables**


> **Note:** Make sure your Docker containers are running before attempting to connect to the database.