package ku.cs.controllers;

import com.github.saacsos.FXRouter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ku.cs.services.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InvoiceController {

    @FXML
    ListView<String> invoiceLV, soLV;

    @FXML
    Button createInvoiceBtn;

    @FXML
    TextField staffIdTF;

    @FXML
    Label customerNameLB, customerTaxIdLB, customerPhoneLB, customerAddressLB, staffNameLB;

    DatabaseConnection dbConnection;
    Connection connection;
    Statement statement;
    ResultSet results;
    String current_so_id;

    public void initialize() throws SQLException {
        dbConnection = new DatabaseConnection();
        connection = dbConnection.getConnection();
        setSOLV(querySaleOrder());
        handleSelectedInvoiceLV();
        handleSelectedSOLV();
        customerNameLB.setText("");
        customerTaxIdLB.setText("");
        customerPhoneLB.setText("");
        customerAddressLB.setText("");
        staffNameLB.setText("");
        invoiceLV.setVisible(false);
        soLV.setVisible(true);
    }

    public void handleBackBtn() throws IOException {
        FXRouter.goTo("home");
    }

    public void handleToInvoiceBtn() throws SQLException {
        customerNameLB.setText("");
        customerTaxIdLB.setText("");
        customerPhoneLB.setText("");
        customerAddressLB.setText("");
        staffNameLB.setText("");

        staffIdTF.setVisible(true);
        createInvoiceBtn.setVisible(true);
        soLV.getItems().clear();
        soLV.refresh();
        setSOLV(querySaleOrder());
        invoiceLV.setVisible(false);
        soLV.setVisible(true);
    }

    public void handleInvoicedBtn() throws SQLException {
        staffIdTF.setVisible(false);
        createInvoiceBtn.setVisible(false);
        invoiceLV.getItems().clear();
        invoiceLV.refresh();
        setInvoiceLV(queryInvoice());
        invoiceLV.setVisible(true);
        soLV.setVisible(false);
    }

    public void handleCreateInvoiceBtn() throws Exception {
        String staff_id = staffIdTF.getText();
        String inv_id;
        String username;
        double totalPrice;
        LocalDate date = LocalDate.now();
        String time = "\"" + date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "\"";
        String query_staff = "SELECT S_ID FROM Staff WHERE S_ID LIKE " + "\"" + staff_id + "\"";
        String query_inv = "SELECT INV_ID FROM Invoice ORDER BY INV_ID DESC";
        String query_username = "SELECT Username FROM Sale_Order WHERE SO_ID LIKE " + "\"" + current_so_id + "\"";
        String query_price = "SELECT SUM(Price) AS Total_Price FROM Sale_Order_Detail WHERE SO_ID LIKE " + "\"" + current_so_id + "\"";

        statement = connection.createStatement();
        results = statement.executeQuery(query_staff);
        System.out.println(query_staff);
        if(!results.next())
            throw new Exception("Can't find staff id : " + staff_id);

        statement = connection.createStatement();
        results = statement.executeQuery(query_username);
        results.next();
        username = results.getString("Username");
        System.out.println(query_username);

        statement = connection.createStatement();
        results = statement.executeQuery(query_price);
        results.next();
        totalPrice = Double.parseDouble(results.getString("Total_Price"));
        System.out.println(query_price);


        statement = connection.createStatement();
        results = statement.executeQuery(query_inv);
        System.out.println(query_inv);
        if(!results.next()){
            inv_id = "\"0001\"";
        }else{
            int id = Integer.parseInt(results.getString("INV_ID")) + 1;
            inv_id = "\"" + String.format("%04d", id) + "\"";
        }

        String insert = "INSERT INTO Invoice(INV_ID, SO_ID, Username, S_ID, Total_Price, INV_Date)" +
                "VALUES(" + inv_id + "," + "\"" + current_so_id + "\"" + "," + "\"" + username + "\"" + "," +  "\"" + staff_id + "\"" + "," + totalPrice + "," + time + ")";
        System.out.println(insert);
        statement = connection.createStatement();
        statement.execute(insert);
        System.out.println(insert);

        String update_so = "UPDATE Sale_Order SET Status = \"Completed\" WHERE SO_ID LIKE " + "\"" + current_so_id + "\"";
        statement = connection.createStatement();
        statement.execute(update_so);
        System.out.println(update_so);
    }

    public void handleSelectedInvoiceLV(){
        invoiceLV.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        System.out.println(newValue);
                        try {
                            setCustomer(newValue);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public void handleSelectedSOLV(){
        soLV.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        current_so_id = newValue;
                        System.out.println(newValue);
                        try {
                            setCustomerSO(newValue);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public void setCustomer(String inv_id) throws SQLException {
        String query_customer = "SELECT C_Name, Tax_ID, C_Phone, Address, S_Name FROM Customer " +
                "JOIN Invoice USING (Username) " +
                "JOIN Staff USING (S_ID) " +
                "WHERE INV_ID LIKE " + "\"" + inv_id + "\"";
        System.out.println(query_customer);
        statement = connection.createStatement();
        results = statement.executeQuery(query_customer);
        results.next();
        customerNameLB.setText("Customer Name : " + results.getString("C_Name"));
        customerTaxIdLB.setText("Tax ID : " + results.getString("Tax_ID"));
        customerPhoneLB.setText("Phone Number : " + results.getString("C_Phone"));
        customerAddressLB.setText("Address : \n"+ results.getString("Address"));
        staffNameLB.setText("Staff Name : " + results.getString("S_Name"));
    }

    public void setCustomerSO(String so_id) throws SQLException {
        String query_customer = "SELECT C_Name, Tax_ID, C_Phone, Address FROM Customer " +
                "JOIN Sale_Order USING (Username) " +
                "WHERE SO_ID LIKE " + "\"" + so_id + "\"";
        System.out.println(query_customer);
        statement = connection.createStatement();
        results = statement.executeQuery(query_customer);
        results.next();
        customerNameLB.setText("Customer Name : " + results.getString("C_Name"));
        customerTaxIdLB.setText("Tax ID : " + results.getString("Tax_ID"));
        customerPhoneLB.setText("Phone Number : " + results.getString("C_Phone"));
        customerAddressLB.setText("Address : \n"+ results.getString("Address"));
    }

    public String querySaleOrder(){
        return "SELECT SO_ID FROM Sale_Order WHERE Status LIKE \"Produced\"";
    }

    public String queryInvoice(){
        return "SELECT INV_ID FROM Invoice";
    }

    public void setSOLV(String query) throws SQLException {
        statement = connection.createStatement();
        results = statement.executeQuery(query);
        while (results.next()){
            String so_id = results.getString("SO_ID");
            soLV.getItems().add(so_id);
        }
        System.out.println(query);
    }

    public void setInvoiceLV(String query) throws SQLException {
        statement = connection.createStatement();
        results = statement.executeQuery(query);
        while (results.next()){
            String so_id = results.getString("INV_ID");
            invoiceLV.getItems().add(so_id);
        }
        System.out.println(query);
    }
}
