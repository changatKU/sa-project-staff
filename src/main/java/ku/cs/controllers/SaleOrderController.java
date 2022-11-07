package ku.cs.controllers;

import com.github.saacsos.FXRouter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import ku.cs.services.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SaleOrderController {
    @FXML
    ListView<String> saleOrderLV, saleOrderDetailLV;

    @FXML
    Label titleLB, soIdLB, customerLB, dateLB, productIdLB, widthLB, lengthLB, gramLB, quantityLB;

    @FXML
    Button acceptBtn, doneBtn;

    DatabaseConnection dbConnection;
    Connection connection;
    Statement statement;
    ResultSet results;

    String current_so_id;

    public void initialize(){
        dbConnection = new DatabaseConnection();
        connection = dbConnection.getConnection();
        doneBtn.setVisible(false);
        acceptBtn.setVisible(true);
        setSaleOrderLV(queryNewSaleOrder());
        handleSelectedSaleOrderLV();
        handleSelectedSaleOrderDetailLV();
    }

    public void handleBackBtn(){
        try {
            FXRouter.goTo("home");
        } catch (IOException e) {
            throw new RuntimeException("Can't go to Home view");
        }
    }

    public void handleNewBtn(){
        doneBtn.setVisible(false);
        acceptBtn.setVisible(true);
        clearSoDetail();
        clearSodDetail();
        saleOrderLV.getItems().clear();
        saleOrderLV.refresh();
        saleOrderDetailLV.getItems().clear();
        saleOrderDetailLV.refresh();
        setSaleOrderLV(queryNewSaleOrder());
    }

    public void handleInProcessBtn(){
        doneBtn.setVisible(true);
        acceptBtn.setVisible(false);
        clearSoDetail();
        clearSodDetail();
        saleOrderLV.getItems().clear();
        saleOrderLV.refresh();
        saleOrderDetailLV.getItems().clear();
        saleOrderDetailLV.refresh();
        setSaleOrderLV(queryInProcessSaleOrder());
    }

    public void handleProducedBtn(){
        doneBtn.setVisible(false);
        acceptBtn.setVisible(false);
        clearSoDetail();
        clearSodDetail();
        saleOrderLV.getItems().clear();
        saleOrderLV.refresh();
        saleOrderDetailLV.getItems().clear();
        saleOrderDetailLV.refresh();
        setSaleOrderLV(queryProducedSaleOrder());
    }
    public void handleAcceptBtn(){
        String update = "UPDATE Sale_Order SET Status = \"In Process\" WHERE SO_ID LIKE " + "\"" + current_so_id +  "\"";
        saleOrderLV.getItems().clear();
        saleOrderLV.refresh();
        saleOrderDetailLV.getItems().clear();
        saleOrderDetailLV.refresh();
        clearSoDetail();
        clearSodDetail();
        setSaleOrderLV(queryNewSaleOrder());
        try {
            statement.execute(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleDoneBtn(){
        String update = "UPDATE Sale_Order SET Status = \"Produced\" WHERE SO_ID LIKE " + "\"" + current_so_id +  "\"";
        saleOrderLV.getItems().clear();
        saleOrderLV.refresh();
        saleOrderDetailLV.getItems().clear();
        saleOrderDetailLV.refresh();
        clearSoDetail();
        clearSodDetail();
        setSaleOrderLV(queryInProcessSaleOrder());
        try {
            statement.execute(update);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    public void handleSelectedSaleOrderLV(){
        saleOrderLV.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        current_so_id = newValue;
                        saleOrderDetailLV.getItems().clear();
                        clearSodDetail();
                        setSaleOrderDetailLV(newValue);
                        setSoDetail(newValue);
                    }
        });
    }

    public void handleSelectedSaleOrderDetailLV(){
        saleOrderDetailLV.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        setSodDetail(newValue);
                    }
                });
    }

    public void setSaleOrderLV(String query){
        System.out.println(query);
        try {
            statement = connection.createStatement();
            results = statement.executeQuery(query);
            while(results.next()){
                String id = results.getString("SO_ID");
                saleOrderLV.getItems().add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSaleOrderDetailLV(String so_id){
        so_id = "\"" + so_id + "\"";
        String query = "SELECT P_ID FROM Sale_Order_Detail " +
                "WHERE SO_ID LIKE " + so_id;
        System.out.println(query);
        try {
            results = statement.executeQuery(query);
            while (results.next()){
                String id = results.getString("P_ID");
                saleOrderDetailLV.getItems().add(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSoDetail(String so_id){
        clearSoDetail();
        titleLB.setText("Sale Order");
        soIdLB.setText("Sale Order ID : " + so_id);
        so_id = "\"" + so_id + "\"";

        String query = "SELECT Username, SO_Date FROM Sale_Order " +
                "JOIN Customer USING (Username) " +
                "WHERE SO_ID LIKE " + so_id;
        System.out.println(query);

        try {
            results = statement.executeQuery(query);
            results.next();
            customerLB.setText("Customer : " + results.getString("Username"));
            dateLB.setText("Date : " + results.getString("SO_Date"));

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSodDetail(String p_id){
        clearSodDetail();
        p_id = "\"" + p_id + "\"";
        String so_id = "\"" + current_so_id + "\"";
        String query = "SELECT P_ID, Width, Length, Gram, Price, QTY " +
                "FROM Sale_Order_Detail " +
                "JOIN Product USING (P_ID) " +
                "WHERE SO_ID LIKE " + so_id +
                " AND P_ID LIKE " + p_id;
        System.out.println(query);
        try {
            results = statement.executeQuery(query);
            results.next();
            productIdLB.setText("Product ID : " + results.getString("P_ID"));
            widthLB.setText("Width : " + results.getString("Width"));
            lengthLB.setText("Length : " + results.getString("Length"));
            gramLB.setText("Gram : " + results.getString("Gram"));
            quantityLB.setText("Quantity : " + results.getString("QTY"));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearSodDetail(){
        productIdLB.setText("");
        widthLB.setText("");
        lengthLB.setText("");
        gramLB.setText("");
        quantityLB.setText("");
    }

    public void clearSoDetail(){
        titleLB.setText("");
        soIdLB.setText("");
        customerLB.setText("");
        dateLB.setText("");
    }

    public String queryNewSaleOrder(){
        return "SELECT SO_ID FROM Sale_Order WHERE Status LIKE \"New\"";
    }

    public String queryInProcessSaleOrder(){
        return "SELECT SO_ID FROM Sale_Order WHERE Status LIKE \"In Process\"";
    }

    public String queryProducedSaleOrder(){
        return "SELECT SO_ID FROM Sale_Order WHERE Status LIKE \"Produced\"";
    }
}
