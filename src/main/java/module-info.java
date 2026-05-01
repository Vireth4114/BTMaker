module btmaker.btmaker {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.graphics;
    requires org.apache.commons.lang3;
    requires java.desktop;
    requires kotlin.stdlib;

    opens btmaker;
    exports btmaker;
    exports btmaker.resources;
    exports commands;
    exports model;
}
