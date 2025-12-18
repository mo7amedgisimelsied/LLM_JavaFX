module com.quizapp {
    requires javafx.controls;
    requires java.sql;

    // Updated module requirement for the new connector
    requires com.mysql.connector.j;

    exports com.quizapp;
}