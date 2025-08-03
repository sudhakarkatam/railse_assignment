# 📦 Workforce Management API - Submission

## ✅ 1. GitHub Repository (Project Code)

🔗 **Repository Link:**  
[https://github.com/sudhakarkatam/railse_assignment](https://github.com/sudhakarkatam/railse_assignment)

> This repository contains the full source code for the Workforce Management Backend Engineer assignment.  
> It includes the starter code, all bug fixes, and newly implemented features as listed in the challenge.

---

## 🎥 2. Video Demonstration

🔗 **Demo Link (Google Drive):**  
[https://drive.google.com/file/d/1RkeXRTvEW_SYH3-Y7W1tRsJfoK-BW37y/view?usp=sharing](https://drive.google.com/file/d/1RkeXRTvEW_SYH3-Y7W1tRsJfoK-BW37y/view?usp=sharing)

> The video walks through:
> - How to run the application
> - Bug fixes for `assign-by-ref` and `fetch-by-date/v2`
> - Implementation and demonstration of all 3 major features

---

## 🛠️ Features Implemented

### ✅ Bug Fixes

- **Bug #1:** `assign-by-ref` now correctly reassigns only the latest open task and cancels the rest.
- **Bug #2:** `fetch-by-date/v2` now filters out cancelled tasks and respects the given date range.

---

### 🚀 New Features

#### 1. Smart Daily Task View
- Enhanced `/fetch-by-date/v2` to:
  - Include all active tasks created **within** the selected date range.
  - Also include tasks created **before** the range but still active (not completed or cancelled).

#### 2. Task Priority Management
- Added a `priority` field to the task model (`HIGH`, `MEDIUM`, `LOW`).
- New endpoint to **update task priority**.
- New endpoint to **fetch tasks by priority**, e.g., `/tasks/priority/HIGH`.

#### 3. Task Comments & Activity History
- Introduced **activity logging** for task events (creation, status updates, priority changes, etc.).
- Added support for **user comments** on tasks.
- Fetching task details now returns:
  - Full **activity history**
  - All **user comments**  
  *(Sorted by timestamp for clarity)*

---

## 🧪 How to Test

1. Clone the repository.
2. Ensure Java 17 and Gradle are installed.
3. Run the main class: `com.railse.hiring.workforcemgmt.Application`
4. Use the provided cURL commands or Postman to test endpoints.

---

📩 Feel free to reach out for any clarifications or walkthroughs.  
Thank you for reviewing the submission!
