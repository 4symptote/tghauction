package com.app.client.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuctionCreateController {

    // Liên kết với các phần tử trong file FXML
    @FXML private TextField titleField;
    @FXML private ComboBox<String> itemTypeComboBox;
    @FXML private TextField startPriceField;
    @FXML private TextField imageUrlField;
    @FXML private TextArea descriptionArea;
    @FXML private Label errorLabel;
    @FXML private Button publishButton;
    @FXML private Button cancelButton;

    @FXML
    public void initialize() {
        // Hàm chạy khi giao diện được nạp, cài đặt mặc định cho ComboBox loại sản phẩm
        itemTypeComboBox.setItems(FXCollections.observableArrayList("Electronics", "Art", "Vehicle"));
        itemTypeComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handlePublish(ActionEvent event) {
        // 1. Thu thập dữ liệu từ giao diện
        String title = titleField.getText();
        String itemType = itemTypeComboBox.getValue();
        String priceText = startPriceField.getText();
        String imageUrl = imageUrlField.getText();
        String description = descriptionArea.getText();

        // 2. Validate dữ liệu nhập
        if (title == null || title.trim().isEmpty()) {
            errorLabel.setText("Lỗi: Vui lòng nhập tên sản phẩm.");
            return;
        }

        double startPrice;
        try {
            startPrice = Double.parseDouble(priceText);
            if (startPrice <= 0) {
                errorLabel.setText("Lỗi: Giá khởi điểm phải lớn hơn 0.");
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Lỗi: Định dạng giá không hợp lệ. Vui lòng nhập số.");
            return;
        }

        /*
           3. LOGIC KẾT NỐI VÀO CỐT LÕI (Sử dụng Factory Method & Trạng thái):
           - Gọi Factory để tạo sản phẩm cụ thể. (Cần triển khai theo sơ đồ lớp của bạn)
           - Tạo Phiên đấu giá và đặt trạng thái "OPEN"
        */

        // Item newItem = ItemCreator.createItem(itemType.toLowerCase(), title, description, startPrice, imageUrl);
        // String currentSellerId = SessionManager.getCurrentUser().getUid();
        // Auction newAuction = new Auction(newItem, "OPEN", currentSellerId);

        /*
           4. Gửi Request qua Socket về Server để lưu trữ
           - Yêu cầu ứng dụng của bạn phải có 1 ServerConnection/SocketManager sẵn
        */
        // ServerConnection.getInstance().sendRequest(new CreateAuctionRequest(newAuction));

        System.out.println("Gửi yêu cầu tạo Auction thành công: " + title + " - Giá: " + startPrice);

        // 5. Thành công -> Đóng cửa sổ và clear lỗi
        errorLabel.setText("");
        closeWindow();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        // Khi nhấn Cancel, đóng popup đi
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}