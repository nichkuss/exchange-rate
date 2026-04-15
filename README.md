## 👤 Personalization

- GitHub Username: **nichkuss**
- Spread Factor: **0.00872**

> Note: The Spread Factor above must match the exact value returned by your function.

## 🚀 Setup / Run Instructions
### 1. Clone the repository from master branch
```bash
git clone https://github.com/YOUR_GITHUB_USERNAME/YOUR_REPO.git
cd YOUR_REPO
```

### 2. Build the application
- Right click on project
- Maven -> Update Project -> Check List on Force Update of Snapshots / Releases -> OK

### 3. Run the test
- Go to /src/test/java
- Open the test file (example: ExchangeRateApplicationTests.java)
- Click Run button

### 4. Run the application
- Right click on project
- Run As -> Spring Boot App

### 5. Endpoint usage
- Latest IDR Rates
```bash
curl http://localhost:8080/api/exchange-rate/data/latest_idr_rates
```

- Historical IDR USD
```bash
curl http://localhost:8080/api/exchange-rate/data/historical_idr_usd
```

- Supported Currencies
```bash
http://localhost:8080/api/exchange-rate/data/supported_currencies
```
