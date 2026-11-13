# Prompt Quality Scorer

**An Android application that improves software requirements before they are submitted to AI coding tools.**

## 1. Project Overview

Prompt Quality Scorer is an Android application developed as part of the ITMDA3 project. Its purpose is to help developers improve the quality and completeness of software requirements before submitting them to AI coding assistants such as ChatGPT, GitHub Copilot and Gemini Code Assist.

Users can enter an informal or incomplete software requirement, and the application analyses it using a locally hosted language model through LM Studio. It provides a completeness score, identifies missing information and generates an improved technical specification in Markdown format.

The application also allows users to enable company-specific standards so that the analysis considers relevant organisational requirements and practices. Previous analyses are saved locally and can be accessed through the History screen.

## 2. Main Features

- **Requirement Analysis:** Enter informal software requirements for analysis.
- **Completeness Scoring:** Receive a score between 0 and 100 indicating how complete the requirement is.
- **Missing Requirement Identification:** View gaps, ambiguities and missing information identified by the language model.
- **Improved Specifications:** Generate a structured technical specification in Markdown format.
- **Company Standards:** Enable or disable company-specific rules to influence the analysis.
- **Analysis History:** Access previously completed analyses stored locally on the device.
- **Upgraded Scoring:** View a second completeness score for the improved specification.
- **Loading and Cancellation:** View a loading screen while the model processes a request and cancel an analysis if necessary.

## 3. Technologies Used

| Technology | Purpose |
|---|---|
| Java 11 | Main application programming language |
| XML | Android user interface layouts |
| Android Studio | Application development environment |
| Room / SQLite | Local database storage |
| OkHttp 4.12.0 | HTTP communication with LM Studio |
| Gson 2.10.1 | JSON processing |
| Material Components 1.10.0 | User interface components |
| Android Jetpack ViewModel | Managing application state |
| LM Studio | Hosting the local language model |
| Gemma 4 26B-A4B | Language model used during development and testing |
| Gradle | Project build automation |

## 4. Application Architecture

The application uses a layered structure to separate the user interface, application logic, network communication and local data storage.

The main components include:

- **MainActivity:** Manages the application's screens and navigation.
- **MainViewModel:** Manages application state and coordinates operations.
- **PromptAnalyzerService:** Sends analysis requests to the locally hosted language model.
- **AnalysisResponseParser:** Validates and processes the JSON response returned by the model.
- **Room Database:** Stores previous analyses and company standards.
- **Data Access Objects (DAOs):** Handle database queries and data operations.

The application communicates with LM Studio through its OpenAI-compatible API endpoint:

`http://10.0.2.2:1234/v1/chat/completions`

## 5. System Requirements

Before running the application, ensure that you have the following:

- Android Studio installed.
- An Android emulator running Android 7.0 (API 24) or newer.
- Java 11.
- LM Studio installed.
- A compatible instruction-following language model downloaded in LM Studio.
- Sufficient system memory and hardware resources to run the selected language model.

**Recommended model:** Gemma 4 26B-A4B, which was used during the development and demonstration of the application.

Smaller compatible models may also be used depending on the available hardware.

## 6. Installation and Setup

### Step 1: Download the project

Clone the GitHub repository:

```bash
git clone https://github.com/PixelAndPotion/PromptQualityScorer.git
```

Open the downloaded project in Android Studio.

### Step 2: Configure LM Studio

1. Download and install LM Studio from https://lmstudio.ai/.
2. Download a compatible instruction-following language model.
3. Load the selected model.
4. Start the local server using port `1234`.
5. Ensure that the OpenAI-compatible API server is running.

### Step 3: Configure the Android Emulator

1. Open Android Studio.
2. Navigate to Device Manager.
3. Start an Android emulator running API 24 or higher.
4. Ensure that the emulator is running before launching the application.

### Step 4: Build and Run

1. Allow Android Studio to synchronise the Gradle project.
2. Select the Android emulator as the deployment device.
3. Click Run to build and install the application.
4. Once installed, open Prompt Quality Scorer.

### Step 5: Use the Application

1. Enter a software requirement on the Home screen.
2. Navigate to Company Profile Standards if you want to enable specific rules.
3. Select the relevant standards.
4. Return to the Home screen.
5. Select **Analyse and Score**.
6. Wait for the language model to process the request.
7. Review the completeness score and identified gaps.
8. View the improved technical specification.
9. Access previous analyses through the History screen.

## 7. Installing the APK

A pre-built APK is available under the GitHub repository's Releases section.

**APK file:** `Test_Application.apk`

To install the application directly onto an Android emulator:

```bash
adb install Test_Application.apk
```

Alternatively, drag the APK file into the running Android emulator.

The submitted application details are:

| Property | Value |
|---|---|
| Application name | Prompt Quality Scorer |
| Version | 1.0 |
| Version code | 1 |
| Package name | com.example.test_application |
| Minimum Android version | Android 7.0 (API 24) |
| Target API | 37 |
| APK size | 6.66 MB |

## 8. Important Limitations

The following limitations should be considered when running the application:

- **Emulator networking:** The current server address, `10.0.2.2`, is specifically used to allow the Android emulator to communicate with LM Studio running on the host computer.
- **Physical devices:** The current configuration does not connect directly to a physical Android phone. The server address must be changed to the host computer's local network IP address, and the application must be rebuilt.
- **Model performance:** Processing times depend on the selected language model and the computer's hardware. Larger models may take considerably longer to generate responses.
- **Scoring consistency:** Scores are generated by the language model and may vary between requests or models.
- **Local server requirement:** LM Studio must be running and the selected model must be loaded before analysis can take place.
- **Data storage:** Analysis history and company standards are stored locally using Room/SQLite. Analyses are saved automatically, and history deletion is not currently supported.
- **Response formatting:** Some models may return incorrectly formatted JSON or HTML content, which can affect how specifications are displayed.

## 9. Testing

The application was tested using two testers and two software requirement scenarios:

- Campus Equipment Booking.
- Workshop Booking.

Testing included functional tests, usability tests, input validation, error handling and company standards.

A total of **18 test executions passed without application crashes**.

Testing also identified areas for improvement, including inconsistent model-generated scores, response times and occasional incorrectly formatted model responses.

## 10. Future Improvements

Potential improvements for future versions include:

- Guided questions to help users improve incomplete requirements.
- Requirement versioning, tagging and search functionality.
- Editing and deleting saved analyses.
- Exporting specifications to Markdown and PDF.
- Configurable LM Studio server settings.
- Improved scoring consistency using a fixed assessment checklist.
- Streaming model responses to reduce waiting times.
- Support for physical Android devices.
- Additional security through encrypted storage and secure network communication.
- Automated unit and interface testing.

## 11. Project Resources

- **GitHub Repository:** https://github.com/PixelAndPotion/PromptQualityScorer
- **APK Download:** Available under the repository's Releases section.
- **Video Walkthrough:** https://1drv.ms/v/c/34e61c19fdd0c6c1/IQAYHuXoolriRJhpqBATgfZQAQTqFWBsnqE8GpWRA9_iGXg?e=5gMJXR
- **Wireframes:** Available in `WIREFRAMES.md` in the project repository.

## 12. Project Team

Developed as part of the ITMDA3 group project by **PixelAndPotion**.

**Project:** Prompt Quality Scorer  
**Application Version:** 1.0  
**Development Year:** 2026

---
