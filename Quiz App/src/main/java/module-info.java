module com.quizapp {
    requires javafx.controls;
    requires java.sql;

    // Correct module name for com.mysql:mysql-connector-j artifact
    requires mysql.connector.j;

    exports com.quizapp;
}