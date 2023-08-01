module BTMaker.BTMaker {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
	requires transitive javafx.graphics;
	requires zip4j;

    opens BTMaker.BTMaker;
    exports BTMaker.BTMaker;
}
