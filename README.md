# Database Drift Detector for Oracle & JPA

A command-line tool to detect and report schema drift between an Oracle database and Java Persistence API (JPA) entities.

---

## Overview

This application provides a robust solution for DevOps and development teams to maintain schema consistency. It works by generating two separate, canonical YAML files: one representing the live Oracle database schema and the other representing the schema as defined by your Java JPA entities.

By comparing these two files with a standard `diff` tool, you can instantly identify any discrepancies, such as missing columns, name mismatches, or unapplied migrations. This helps prevent runtime errors and ensures that your application's data model is perfectly synchronized with the database.

---

## Features

* **Database Introspection:** Connects to an Oracle database and extracts table and column metadata directly from the system catalog.
* **Advanced JPA Parsing:**
    * Parses Java source files to understand your entity model.
    * Correctly handles complex inheritance hierarchies (`@MappedSuperclass`).
    * Supports embedded objects (`@Embedded`, `@Embeddable`).
    * Respects column name overrides (`@AttributeOverride`).
* **Canonical Output:** Generates clean, alphabetically sorted YAML files for easy and reliable comparison.
* **Self-Contained:** Built as a single executable "fat-jar," making it easy to run in any environment.
* **Secure:** Prompts for the database password interactively so it doesn't have to be stored in scripts or command history.

---

## Prerequisites

To build and run this project, you will need:
* **JDK 24** or newer.
* **Apache Maven** (the project includes a Maven Wrapper, so a local installation is not strictly required).
* Network access to the target Oracle database.

---

## Build Instructions

The project uses the Maven Wrapper, which simplifies the build process.

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd database-drift-detector
    ```

2.  **Build the project using the wrapper:**
    * On Linux or macOS:
        ```bash
        ./mvnw clean install
        ```
    * On Windows:
        ```bash
        mvnw.cmd clean install
        ```

3.  After a successful build, the executable JAR file will be located in the `target/` directory: `target/database-drift-detector-1.0.0.jar`.

---

## Usage

The application is controlled via command-line arguments. Run the JAR file using `java -jar` and provide the necessary options.

### Command-Line Options

| Argument | Required | Description |
| :--- | :--- | :--- |
| `--source-dir` | **Yes** | The path to the root directory of your Java source code (e.g., `/path/to/project/src/main/java`). |
| `--db-type` | **Yes** | The type of the database. Currently, only `oracle` is supported. |
| `--host` | **Yes** | The hostname or IP address of the Oracle database server. |
| `--port` | **Yes** | The port number for the Oracle listener (e.g., `1521`). |
| `--user` | **Yes** | The username for connecting to the database. |
| `--password` | **Yes** | The password for the database user. The application will prompt for this interactively for security. |
| `--dbname` | **Yes** | The Oracle Service Name or SID for the database connection. |
| `--schema` | **Yes** | The Oracle schema (user) that owns the tables you want to inspect (e.g., `HR`). This value is case-insensitive and will be converted to uppercase. |

### Example

Here is a complete example of how to run the application. You can copy this command, replace the placeholder values, and run it in your terminal from the project's root directory.

```bash
java -jar target/database-drift-detector-1.0.0.jar \
    --source-dir "/path/to/your/java/project/src/main/java" \
    --db-type "oracle" \
    --host "your-db-host.example.com" \
    --port "1521" \
    --user "YOUR_DB_USER" \
    --dbname "YOUR_DB_SERVICE_NAME" \
    --schema "YOUR_SCHEMA_NAME"
