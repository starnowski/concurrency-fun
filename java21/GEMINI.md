# Project Context: Java Concurrency Fun

## Overview
This project is a playground and library for experimenting with Java concurrency patterns and testing methodologies. It specifically focuses on implementing and verifying thread-safe components like **Rate Limiters** and **Atomic Reference** manipulations using Java 17.

The project relies heavily on **jcstress** (Java Concurrency Stress tests) to verify correctness under heavy concurrency, alongside standard unit and functional tests using **Spock** and **MultithreadedTC**.

## Technology Stack
*   **Language:** Java 17 (enforced via Maven Toolchains)
*   **Build System:** Maven
*   **Testing (Unit/Functional):** Spock Framework (Groovy), JUnit 5, MultithreadedTC
*   **Testing (Concurrency):** OpenJDK jcstress

## Project Structure & Modules
The project is organized as a multi-module Maven build:

*   **`core`**: Contains the main application logic and interfaces (e.g., `RateLimiter`, `Account`, `TemporaryValueStore`). Also includes standard unit and functional tests.
*   **`jcstress-tests`**: Contains jcstress test suites specifically for the Rate Limiter implementations.
*   **`jcstress-atomicreference`**: Contains jcstress test suites for Atomic Reference patterns (e.g., `CheckingAccount` tests).
*   **`jcstress-temporary-value-tests`**: Contains jcstress test suites for the `TemporaryValueStore` component.

## Building and Running

### Prerequisites
*   JDK 17 installed and configured.
*   Maven installed.

### Standard Build & Unit Tests
To build the entire project and run the standard unit/functional tests (Spock/JUnit):

```bash
./mvnw clean install
```

*(On Windows, use `.\mvnw.cmd`)*

### Running Concurrency Stress Tests (jcstress)
The jcstress tests are packaged into executable JARs within their respective modules.

1.  **Build the project** (if not already done):
    ```bash
    ./mvnw clean package
    ```

2.  **Run the stress tests** for a specific module.
    
    *   **Rate Limiter Tests:**
        ```bash
        java -jar jcstress-tests/target/jcstress.jar
        ```
    
    *   **Atomic Reference Tests:**
        ```bash
        java -jar jcstress-atomicreference/target/jcstress.jar
        ```

    *   **Temporary Value Store Tests:**
        ```bash
        java -jar jcstress-temporary-value-tests/target/jcstress.jar
        ```

    *Note: jcstress tests can take a significant amount of time to run. You can list specific tests to run by passing arguments to the JAR, or use `-h` for help.*

## Development Conventions
*   **Java Version:** Ensure you are using Java 17. The build may fail if the toolchain is not correctly detected.
*   **Testing:**
    *   **Logic Verification:** Use Spock (Groovy) for testing business logic and single-threaded behavior. Files are located in `src/test/groovy`.
    *   **Thread Safety:** Use jcstress for proving thread safety. These tests are annotated with `@JCStressTest`, `@Actor`, and `@Arbiter`.
*   **Code Style:** Follow standard Java conventions.
