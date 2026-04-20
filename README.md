# 🚗 Transport Management System

A robust, Java-based Desktop Application designed to efficiently manage transport operations. The system provides complete CRUD (Create, Read, Update, Delete) functionality for managing multiple aspects of transportation, including Vehicles, Drivers, and Trips. 

This project includes both a **Graphical User Interface (GUI)** built utilizing Java Swing and a **Command Line Interface (CLI)**, both backed by a MySQL database for reliable data storage.

## ✨ Features

* **Dual Interfaces:** Contains both a seamless GUI (`TransportGUI`) and a fast CLI (`TransportManagementSystem`).
* **Vehicle Management:** Add new vehicles, modify their capacities, and remove decommissioned vehicles.
* **Driver Management:** Keep track of driver records, including IDs, names, and contact details.
* **Trip Records:** Log and manage trips, associating them with specific vehicles, mapping distances, and tracking dates.
* **CSV Export:** Easily download and export any grid data (Vehicles, Drivers, Trips) into an Excel-friendly CSV format directly from the GUI.
* **Automatic Database Setup:** The CLI version includes startup logic to automatically build the necessary MySQL tables if they don't already exist.

## 🛠️ Technology Stack

* **Programming Language:** Java (JDK 8 or higher)
* **User Interface:** Java Swing & AWT
* **Database:** MySQL
* **Connectivity:** JDBC (Java Database Connectivity)

## 📋 Prerequisites

Before running the application, assure you have the following installed:
1. **Java Development Kit (JDK):** Version 8 or newer.
2. **MySQL Server:** Running locally on port `3306`.
3. **MySQL JDBC Driver:** `mysql-connector-java.jar` downloaded and added to your classpath.

## 🗄️ Database Setup

1. Open your MySQL command-line tool or MySQL Workbench.
2. Create the core database:
   ```sql
   CREATE DATABASE transport;
