module BTMaker.BTMaker {
    requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.graphics;
	requires zip4j;
	requires org.apache.commons.lang3;

    opens BTMaker.BTMaker;
    exports BTMaker.BTMaker;
    exports model;
}
