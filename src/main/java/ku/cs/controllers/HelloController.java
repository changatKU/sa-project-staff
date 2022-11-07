package ku.cs.controllers;

import com.github.saacsos.FXRouter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class HelloController {
    public void handleNewSaleOrderBtn(){
        try {
            FXRouter.goTo("sale-order");
        } catch (IOException e) {
            throw new RuntimeException("Can't go to Sale Order view");
        }
    }

    public void handleInvoiceBtn(){
        try {
            FXRouter.goTo("invoice");
        } catch (IOException e) {
            throw new RuntimeException("Can't go to Invoice view");
        }
    }
}