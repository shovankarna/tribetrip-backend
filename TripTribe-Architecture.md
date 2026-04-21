# TripTribe Application Architecture

Below is the holistic architecture diagram for the microservices-based version of TripTribe. This diagram visualizes the flow of data from the end-user's browser, through the infrastructure API Gateway, into the isolated backend Spring Boot services, and finally down to the persistent database layers. 

```mermaid
flowchart TD
    %% Define Global Styles
    classDef client fill:#3498db,stroke:#2980b9,stroke-width:2px,color:#fff
    classDef proxy fill:#f39c12,stroke:#d35400,stroke-width:2px,color:#fff
    classDef auth fill:#9b59b6,stroke:#8e44ad,stroke-width:2px,color:#fff
    classDef service fill:#2ecc71,stroke:#27ae60,stroke-width:2px,color:#fff
    classDef db fill:#e74c3c,stroke:#c0392b,stroke-width:2px,color:#fff
    classDef external fill:#34495e,stroke:#2c3e50,stroke-width:2px,color:#fff

    User((User))

    subgraph Frontend Ecosystem
        UI["React SPA Frontend<br/>(Vite Router)"]:::client
    end

    User -->|Interacts via Browser| UI

    subgraph Infrastructure [Docker Orchestration Network]
        Gateway["NGINX<br/>API Gateway (:80)"]:::proxy
        
        subgraph Auth Layer
            KC["Keycloak<br/>Identity Provider (:8080)"]:::auth
        end

        subgraph Core Domain Microservices
            US["User Service (:8081)"]:::service
            TS["Trip Service (:8082)"]:::service
            IS["Itinerary Service (:8083)"]:::service
            ES["Expense Service (:8084)"]:::service
        end

        subgraph Persistence Layer
            DB[("PostgreSQL Server Cluster (:5432)")]:::db
            PGAdmin["pgAdmin Web Console (:5050)"]:::db
        end
    end

    subgraph External APIs
        Gemini["Google Gemini LLM"]:::external
    end

    %% Network Routing Paths
    UI -->|Redirects /auth/* OIDC| Gateway
    UI -->|Transmits REST /api/*| Gateway

    Gateway -->|Reverse Proxy| KC
    Gateway -->|Routes /api/user| US
    Gateway -->|Routes /api/trips| TS
    Gateway -->|Routes /api/itineraries| IS
    Gateway -->|Routes /api/expenses| ES

    %% Internal Microservice Cross-Communication
    IS -.->|Internal HTTP: Queries Trip Permissions| TS
    ES -.->|Internal HTTP: Queries Trip Permissions| TS

    %% Database Connections
    KC -->|Reads/Writes Auth Tiers| DB
    US -->|Persists Cached Profiles| DB
    TS -->|Persists Trips & RBAC| DB
    IS -->|Persists Templates & Dates| DB
    ES -->|Persists Finances & Balances| DB
    
    PGAdmin -.->|Management Diagnostics| DB

    %% External Communication
    IS -->|Outbound Prompts & Constraints| Gemini
    Gemini -.->|Inbound JSON Schedules| IS
```

## Architectural Highlights
1. **The Infrastructure Perimeter:** External users never hit the Java microservices directly. All external payloads must pass through the `NGINX API Gateway`, which acts as a traffic router.
2. **Stateless JWTs:** `Keycloak` handles generating OAuth tokens. Once the React client has the token, it attaches it to every subsequent `/api/*` call. The individual Java microservices (`User`, `Trip`, `Itinerary`, `Expense`) do not connect back to Keycloak line-by-line; they cryptographically verify the token signatures locally for massive speed enhancements.
3. **Internal Container Networking:** The `Itinerary` and `Expense` services feature dotted logical lines connecting to the `Trip Service`. Without exposing their vulnerabilities to NGINX, they securely ping the Trip Service internally on the Docker network to resolve permission hierarchies (e.g., verifying if a user is an `OWNER` before letting them delete an expense).
4. **Segregated Persistence:** While practically sharing the same Postgres server instance locally to save RAM, the logic operates natively as if each microservice commands its own distinct database schema.
