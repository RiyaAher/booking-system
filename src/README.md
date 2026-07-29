# ⚽ Turf Arena — AI Pitch Concierge & Booking System

An end-to-end Spring Boot application powered by **Spring AI** and **Ollama (`llama3.2`)**, featuring a real-time booking ledger, administrative security, **Vector-based RAG (Retrieval-Augmented Generation)** for policy Q&A, and **LLM Function Calling** for live pitch availability and pricing calculations.

---

## 🌟 Key Features & AI Architecture

### 🤖 1. AI Pitch Concierge (Hybrid RAG + Tool Execution)
* **Policy RAG Search:** Ingests local policy documents (`turf-rules.txt`) into a `SimpleVectorStore` using `nomic-embed-text` embeddings. Answers user queries on footwear, cancellation rules, and amenities without hallucination.
* **LLM Tool / Function Calling:** Directly executes Java `@Bean` methods (`checkAvailabilityTool`, `calculatePriceTool`) to query the active H2/JPA database ledger in real time when users ask about time slots or pricing.
* **Hybrid Reasoning:** Seamlessly combines retrieved vector policy context with live function calling outputs in a single generated response.

### 📅 2. Pitch Booking & Administrative Ledger
* **Live Schedule Ledger:** Interactive dashboard showing real-time slot allocations.
* **Role-Based Access Control (Spring Security):** Guest users can request availability and place reservations; active Admins unlock cancellation and ledger control privileges.
* **Smart UI Integration:** Built-in **Quick Suggestions** chips to demonstrate RAG, tool calling, and hybrid capabilities in one click.

---

## 🛠️ Tech Stack

* **Backend Framework:** Spring Boot 3.x
* **AI Framework:** Spring AI (`spring-ai-ollama-spring-boot-starter`)
* **LLM Engine:** Ollama (`llama3.2`)
* **Embedding Model:** `nomic-embed-text`
* **Vector Store:** Spring AI `SimpleVectorStore` (JSON File Persistence)
* **Database & Persistence:** H2 In-Memory DB / Spring Data JPA
* **Frontend:** Thymeleaf, HTML5, CSS3 (CSS Variables), JavaScript, Flatpickr

---

## 🏗️ AI System Architecture Flow

```text
 User Query (UI / Suggested Chips)
          │
          ▼
   TurfAiService
          │
  ┌───────┴────────────────────────┐
  │                                │
  ▼                                ▼
[1. RAG Vector Search]   [2. LLM Function Calling]
  ├─ Query VectorStore     ├─ checkAvailabilityTool()
  └─ Load Policy Snippet  └─ calculatePriceTool()
  │                                │
  └───────┬────────────────────────┘
          │
          ▼
   Ollama (llama3.2)
          │
          ▼
 Integrated Response Delivered to UI
```

---

## 🚀 Quick Start & Local Setup

### Prerequisites
1. **Java 17+**
2. **Maven 3.8+**
3. **[Ollama](https://ollama.com/) installed and running locally.**

### Step 1: Pull Required Ollama Models
Open your terminal and run:

```bash
# Pull the chat model
ollama pull llama3.2

# Pull the embedding model for RAG
ollama pull nomic-embed-text
```

### Step 2: Clone & Configure
```bash
git clone [https://github.com/YOUR_USERNAME/turf-booking-system.git](https://github.com/YOUR_USERNAME/turf-booking-system.git)
cd turf-booking-system
```

Verify your `src/main/resources/application.properties` contains:
```properties
spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=llama3.2
spring.ai.ollama.embedding.options.model=nomic-embed-text
```

### Step 3: Run the Application
```bash
mvn spring-boot:run
```

Navigate to **`http://localhost:8080`** in your browser.

---

## 🧪 Testing the AI Capabilities

You can test all AI capabilities directly using the **Suggested Questions** chips on the dashboard:

| Chip / Prompt | Target Capability |
| :--- | :--- |
| **"Check Court 1 availability tomorrow (5 PM)"** | Triggers `checkAvailabilityTool` (Function Calling) |
| **"Cancellation & refund policy"** | Vector Search retrieval from `turf-rules.txt` (RAG) |
| **"Footwear rules & Sunday 2-hr pricing"** | Combined RAG search + `calculatePriceTool` execution |

---

## 🔒 Security Configuration

* **Public User Routes:** View dashboard, submit bookings, chat with AI Concierge.
* **Admin Routes:** Log in at `/login` to unlock booking cancellation rights (`hasRole('ADMIN')`).