import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.awt.*;
import javax.swing.*;

import java.io.BufferedWriter;
import java.io.FileWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;

import java.net.URI;

import java.nio.file.*;
import java.util.*;
import org.json.*;

public class App {
    private static final String FILE_PATH = "src/data/contacts.json";

    @FXML
    public Button button1;
    public Button button2;

    @FXML
    public VBox box1;
    public VBox box2;

    @FXML
    public TextField cName;
    public TextField cNumber;

    @FXML
    public Label nameLabel;
    public Label numberLabel;

    @FXML
    public VBox contacts;

    private List<Button> buttons;
    private List<VBox> boxes;

    @FXML
    public void initialize() {
        buttons = List.of(button1, button2);
        boxes = List.of(box1, box2);

        cNumber.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cNumber.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    private void handleButtonClick(javafx.event.ActionEvent event) {
        try {
            for (Button btn : buttons) {
                btn.getStyleClass().remove("selected");
            }

            Button clickedButton = (Button) event.getSource();
            clickedButton.getStyleClass().add("selected");


            for (VBox box : boxes) {
                box.setVisible(false);
                box.setManaged(false);
            }

            int index = buttons.indexOf(clickedButton);
            System.out.println("index " + index);

            if (index == 1) showAllContacts();

            if (index >= 0 && index < boxes.size()) {
                boxes.get(index).setVisible(true);
                boxes.get(index).setManaged(true);
            } else {
                System.out.println("invalid index: " + index);
            }

        } catch (Exception e) {
            System.out.println("error " + e);
        }
    }

    public void handleGitHub(ActionEvent event) {
        try {
            URI uri = new URI("https://github.com/boltHack");
            Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            System.out.println("error " + e);
        }
    }

    public void saveBtn() {
        try {
            if (!cName.getText().isEmpty() && !cNumber.getText().isEmpty()) {

                Path path = Paths.get(FILE_PATH);
                List<String> contacts = new ArrayList<>();

                if (Files.exists(path)) {
                    String content = new String(Files.readAllBytes(path)).trim();
                    if (content.startsWith("[") && content.endsWith("]")) {
                        content = content.substring(1, content.length() - 1).trim();
                        if (!content.isEmpty()) {
                            String[] objs = content.split("},\\s*\\{");
                            for (String obj : objs) {
                                if (!obj.startsWith("{")) obj = "{" + obj;
                                if (!obj.endsWith("}")) obj = obj + "}";
                                contacts.add(obj);
                            }
                        }
                    }
                }

                String newContact = "{\"name\":\"" + cName.getText() + "\",\"number\":\"" + cNumber.getText().replace("\\", "") + "\"}";
                contacts.add(newContact);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
                    bw.write("[");
                    bw.write(String.join(",", contacts));
                    bw.write("]");
                }

                JOptionPane.showMessageDialog(null, "Новый контакт " + cName.getText() + " успешно добавлен!", "Выполнено!", JOptionPane.INFORMATION_MESSAGE);

                cName.setText("");
                cNumber.setText("");

                String content = new String(Files.readAllBytes(path));
                System.out.println("Содержимое JSON-файла:\n" + content);

            } else {
                JOptionPane.showMessageDialog(null, "Пожалуйста, заполните все поля ввода", "Ошибка!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAllContacts() {
        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));

            content = content.trim();
            if (content.startsWith("[")) content = content.substring(1);
            if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

            String[] contactStrings = content.split("\\},\\s*\\{");

            contacts.getChildren().clear();

            for (String cs : contactStrings) {
                if (!cs.startsWith("{")) cs = "{" + cs;
                if (!cs.endsWith("}")) cs = cs + "}";

                String name = cs.split("\"name\"\\s*:\\s*\"")[1].split("\"")[0];
                String number = cs.split("\"number\"\\s*:\\s*\"")[1].split("\"")[0];

                HBox row = new HBox(10);

                Label nameLabel = new Label(name);
                Label numberLabel = new Label(number);
                Button deleteButton = new Button("Удалить");
                deleteButton.getStyleClass().add("delete-btn");

                row.getChildren().addAll(nameLabel, numberLabel, deleteButton);
                contacts.getChildren().add(row);

                deleteButton.setOnMouseClicked(event -> {
                    System.out.println("click " + name);
                    deleteContact(name, number);
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteContact(String deleteName, String deleteNumber) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));

            JSONArray contacts = new JSONArray(content);

            JSONArray updated = new JSONArray();

            for (int i = 0; i < contacts.length(); i++) {
                JSONObject obj = contacts.getJSONObject(i);
                String name = obj.getString("name");
                String number = obj.getString("number");

                if (!(name.equals(deleteName) && number.equals(deleteNumber))) {
                    updated.put(obj);
                }
            }

            Files.write(Paths.get(FILE_PATH), updated.toString(4).getBytes());

            System.out.println("Контакт удалён: " + deleteName + " " + deleteNumber);

            showAllContacts();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
