# Real-Estate-Market-Analysis-Application

## Running the project (using docker compose)

Create .env file based on .env_tamplate
```
POSTGRES_USER=your_db_username
POSTGRES_PASSWORD=your_db_password
POSTGRES_DB=your_db_name

JWT_KEY=your_jwt_secret_key
JWT_EXPIRATION=your_jwt_expiration_time
```

To set JWT_KEY use command in cmd and paste to .env file
```
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }) -as [byte[]])
```

### without building
```
docker compose --env-file <file_name>.env up
```

### with building
```
docker compose --env-file <file_name>.env up --build
```