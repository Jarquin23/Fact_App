module ni.edu.uam.facturacion {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    requires java.sql;
    requires org.postgresql.jdbc;

    exports ni.edu.uam.facturacion.application;
    exports ni.edu.uam.facturacion.model;
    exports ni.edu.uam.facturacion.dao;
    exports ni.edu.uam.facturacion.util;

    opens ni.edu.uam.facturacion.controller to javafx.fxml;
    opens ni.edu.uam.facturacion.model to javafx.base;
}