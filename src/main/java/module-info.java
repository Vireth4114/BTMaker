module BTMaker.BTMaker {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.graphics;
    requires org.apache.commons.lang3;
    requires java.desktop;

    opens BTMaker.BTMaker;
    exports BTMaker.BTMaker;
    exports commands;
    exports model;
}
