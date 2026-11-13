# Task List

## 🏗️ Phase 1: Foundation & Architecture
- [x] Configure `build.gradle` dependencies (OkHttp, Gson, Room).
- [x] Set up package structure (`model`, `db`, root package).

## 💾 Phase 2: Data Layer
- [x] Define `RequirementAnalysis` model.
- [x] Define `HistoryEntity` and `HistoryDao` for prompt tracking.
- [x] Define `CompanyRuleEntity` and `CompanyRuleDao` for Company Profile standards.
- [x] Setup `AppDatabase` with Room migration support.

## 🧠 Phase 3: Analysis & Generation Engine
- [x] Implement `PromptAnalyzerService` (Calls `localhost:1234` via `10.0.2.2`).
- [x] Implement dynamic Company Profile context injection in AI System Prompt.

## 📱 Phase 4: User Interface (Material Design)
- [x] Build Main Input & Analysis Results View.
- [x] Build Company Profile Management Dialog & Custom Standard Creator.
- [x] Build Past Prompts History Viewer.
- [x] Implement Copy to Clipboard.
