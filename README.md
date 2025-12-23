# Evaluating the Performance of Modern LLMs in Generating JavaFX Desktop Applications
## Overview
This repository contains source code and experimental data from a research study evaluating Large Language Models (LLMs) in generating JavaFX desktop applications. It assesses how effectively models translate textual and visual specifications into functional code, focusing on code quality (RQ1), initial success rates (RQ2) and visual fidelity (RQ3).

## Repository Structure
The project uses a Branch-per-Experiment strategy (9 total branches). Each branch combines one Model and one Application.

- **Branch Naming:** Branches are named using the format: [Application-Name]-[Model-Name]

- **Models Evaluated:** Gemini-3-pro, Claude-sonnet-4.5, GPT-5.1-codex.

- **Applications:** age-calculator, quiz-app, drawing-app.

- **Commit History:**
    - Commit 1: Initial "Pass@1" attempt generated from the prompt.
    - Commits 2-6: Subsequent attempts incorporating error feedback (compilation/runtime fixes).


## Experimental Logs
- All experimental metadata is stored on the main branch under the [Chat Logs](https://github.com/mo7amedgisimelsied/LLM_JavaFX/tree/main/Chat%20Logs) directory

- the JSON logs record the full interaction history, including input prompts, execution status, failure analysis, and references to UI screenshots.


## Usage

**Requirements:** Java 24, OpenJFX 25.0.1, Maven 3.9.11, MySQL 8.0.43 (Quiz App).

1. Clone the repository:

    ```bash
    git clone https://github.com/mo7amedgisimelsied/LLM_JavaFX
    ```
2. Navigate to the desired experiment branch:

    ```bash
    git checkout age-calculator-gemini
    ```
3. Build with Maven:

    ```bash
    mvn clean install
    ```
4. Run the application:

    ```bash
    mvn javafx:run
    ```