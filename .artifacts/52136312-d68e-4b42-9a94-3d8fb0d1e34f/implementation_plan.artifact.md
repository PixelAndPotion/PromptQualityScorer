# Prompt Quality Scorer & Spec Builder

This project aims to build an Android application that transforms informal user requirements into structured, AI-ready technical specifications by applying formal requirements engineering principles.

## User Review Required

> [!IMPORTANT]
> **Technology Stack Confirmation**: I am planning to use modern Android standards:
> - **Language**: Kotlin
> - **UI**: Jetpack Compose (Material 3)
> - **Architecture**: MVVM (Model-View-ViewModel)
> - **Local Database**: Room (for History and Company Context)
> - **Dependency Injection**: Hilt (recommended for scalability)

Please confirm if this stack aligns with your preferences or if you have specific constraints.

## Proposed Changes

### 🏗️ Foundation & Architecture
#### [NEW] Project Structure & Dependency Setup
- Configure `build.gradle` files with necessary dependencies (Compose, Room, Hilt, Lifecycle).
- Set up the package structure following MVVM principles (`data`, `domain`, `ui`).

### 💾 Data Layer
#### [NEW] Local Persistence (Room)
- `HistoryEntity`: Stores prompt, score, gaps, and generated spec.
- `CompanyContextEntity`: Stores user-defined global requirements (e.g., "PCI-DSS compliant").
- Room Database and DAOs.

### 🧠 Analysis & Generation Engine
#### [NEW] Scoring Logic (Mock Implementation)
- Create a service that takes a string and returns a `ScoreResult` object containing:
    - Overall clarity score.
    - List of identified gaps (e.g., "Missing error handling").
    - Categories (Functional, Non-functional, etc.).

#### [NEW] Spec Assembly Logic
- A component that merges the raw prompt + answered questions + company context into a single Markdown string.

### 📱 User Interface (Jetpack Compose)
#### [NEW] Main Input Screen
- Text field for entering the rough requirement.
- "Analyze" button.

#### [NEW] Analysis Results Screen
- Display scores using progress indicators or gauges.
- List identified gaps clearly.
- "Continue to Elicitation" action.

#### [NEW] Guided Elicitation UI
- A step-by-step flow (or a list of input fields) based on the detected gaps.
- Example: If gap is "Missing Security", show "What security standards apply?"

#### [NEW] Specification Output Screen
- Display the final generated spec in a readable format.
- "Copy to Clipboard" button.

#### [NEW] Settings & History Screens
- **Settings**: Manage Company Context entries.
- **History**: A list of previous prompts and their results.

## Verification Plan

### Automated Tests
- Unit tests for the `SpecAssemblyLogic`.
- Unit tests for the scoring logic (once implemented).
- Room database integration tests.

### Manual Verification
- Verify the flow from Input $\rightarrow$ Analysis $\rightarrow$ Elicitation $\rightarrow$ Final Spec.
- Ensure "Company Context" is correctly injected into the final spec.
- Test "Copy to Clipboard" functionality.
