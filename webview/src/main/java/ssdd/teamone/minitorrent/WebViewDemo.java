package ssdd.teamone.minitorrent;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import ssdd.teamone.minitorrent.backend.CalculatorService;
import ssdd.teamone.minitorrent.backend.FruitsService;

import static ssdd.teamone.minitorrent.Java2JavascriptUtils.connectBackendObject;

public class WebViewDemo extends Application {

    private final String PAGE = "/index.html";

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false"); // enhance fonts
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        createWebView(primaryStage, PAGE);
    }

    private void createWebView(Stage primaryStage, String page) {

        // create the JavaFX webview
        final WebView webView = new WebView();

        // connect the FruitsService instance as "fruitsService"
        // javascript variable
        connectBackendObject(
                webView.getEngine(),
                "fruitsService", new FruitsService());

        // connect the CalculatorService instance as "calculatorService"
        // javascript variable
        connectBackendObject(
                webView.getEngine(),
                "calculatorService", new CalculatorService());

        // show "alert" Javascript messages in stdout (useful to debug)
        webView.getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> arg0) {
                System.err.println("alertwb1: " + arg0.getData());
            }
        });

        // load index.html
        webView.getEngine().load(
                getClass().getResource(page).
                        toExternalForm());

        primaryStage.setScene(new Scene(webView));
        primaryStage.setTitle("WebView with Java backend");
        primaryStage.show();
    }
}
