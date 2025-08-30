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

---

### 2. Build `.jar` files with Maven
Perform packaging for both **web-api** and **gateway-app**:

1. Open the **Maven side panel**  
2. In **Lifecycle**, select `package`  
3. Run it  

This will generate the `.jar` files needed for the application.

---

### 3. Run with Docker Compose

#### ▶️ Without rebuilding images
```bash
docker compose --env-file <file_name>.env up
```

#### 🔄 With rebuilding images
```bash
docker compose --env-file <file_name>.env up --build
```
