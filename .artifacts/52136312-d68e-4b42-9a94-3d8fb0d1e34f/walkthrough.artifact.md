# Technical Specification & Requirements Analysis Walkthrough

The application now supports **Company Profile & Compliance Requirements Analysis**, allowing organizations to define custom compliance and architecture standards (e.g., PCI-DSS, HIPAA, SOC2, custom internal guidelines) that are automatically enforced when analyzing user prompts and generating specifications.

---

## Key Features Implemented

### 1. Company Profile & Compliance Standards Management
- **Company Profile UI**: Tap **Company Profile** on the main screen to open the manager.
- **Default Pre-populated Standards**:
  - **PCI-DSS Compliance Standard**: Mandates TLS 1.3 encryption, tokenization, strict access logging, and prohibits plain-text credit cards.
  - **HIPAA Data Protection Standard**: Mandates encryption at rest and in transit for Protected Health Information (PHI) with strict audit trails.
- **Custom Company Standards**: Users can add new custom standards (Title & Description) or toggle existing standards on/off using switches.
- **Live Status Indicator**: A summary banner on the home screen indicates how many company profile rules are currently active (e.g., `Active Company Profile Standards (2): PCI-DSS Compliance Standard, HIPAA Data Protection Standard`).

---

### 2. Context-Aware Requirements Analysis Engine
When a user inputs a high-level requirement (such as *"Make a payment gateway"* or *"It should be secure"*):
1. **Dynamic Rule Injection**: The app reads all active Company Profile rules from local Room storage and formats them into a structured context string.
2. **System Prompt Engineering**: The `PromptAnalyzerService` sends a system prompt instructing the local AI (running on `localhost:1234`) to act as a **Senior Requirements Engineer**.
3. **Automated Gap Identification**: If a prompt says "it should be secure", but the Company Profile has PCI-DSS or HIPAA enabled, the AI explicitly flags missing company compliance details as **gaps** (e.g. *"Missing PCI-DSS tokenization & TLS 1.3 specification"*).
4. **Specification Transformation**: The generated technical specification in Markdown automatically fulfills the user's requirement while embedding all active company standards.

---

### 3. Local History & Audit Trail
- **Room Persistence**: Every evaluation is saved locally (`HistoryEntity` and `CompanyRuleEntity`).
- **View History**: Users can review past prompt evaluations, view calculated scores, examine flagged gaps, and restore previous specifications.

---

## System Architecture

```
+-------------------------------------------------------------+
|                        MainActivity                         |
+------------------------------+------------------------------+
                               |
            +------------------+------------------+
            |                                     |
            v                                     v
+-----------------------+             +-----------------------+
|  Company Profile Manager |             |  History & Restore DB |
|  (CompanyRuleEntity)  |             |    (HistoryEntity)    |
+-----------+-----------+             +-----------+-----------+
            |                                     |
            +------------------+------------------+
                               |
                               v
               +-------------------------------+
               |    PromptAnalyzerService      |
               |  (http://10.0.2.2:1234/v1/...) |
               +---------------+---------------+
                               |
                               v
               +-------------------------------+
               |    Local AI Agent / LLM       |
               +-------------------------------+
```

---

## How to Test Company Profile Analysis
1. Tap **Company Profile** on the main screen. Verify that **PCI-DSS Compliance** and **HIPAA Data Protection** are enabled.
2. Enter an informal prompt: *"Build a payment processing service"*.
3. Click **Analyze & Score**.
4. Observe the results:
   - **Gaps**: The AI flags missing compliance items specific to PCI-DSS (e.g., missing tokenization or TLS 1.3 details).
   - **Spec**: The generated specification incorporates full PCI-DSS requirements into the technical document.
5. Tap **Company Profile**, toggle PCI-DSS off, add a custom standard (e.g., *"ISO 27001 Security - Mandatory MFA"*), re-run analysis, and observe how the AI dynamically adapts its requirement analysis lens!
