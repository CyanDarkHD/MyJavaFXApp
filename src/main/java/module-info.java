module cn.cyandark.app {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.geotools.main;
    requires org.geotools.api;
    requires org.geotools.geojson;
    requires org.geotools.shapefile;
    requires org.apache.commons.collections4;
    requires org.geotools.metadata;
    requires org.geotools.referencing;
    requires cn.hutool;
    requires org.geotools.opengis;
    requires poi;
    requires poi.ooxml;
    requires org.locationtech.jts;
    requires com.google.gson;

    opens cn.cyandark.app to javafx.fxml;
    exports cn.cyandark.app;
}