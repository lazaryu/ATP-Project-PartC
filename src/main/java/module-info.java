module com.example.atpprojectpartc {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.atpprojectpartc to javafx.fxml;
    exports com.example.atpprojectpartc;
}