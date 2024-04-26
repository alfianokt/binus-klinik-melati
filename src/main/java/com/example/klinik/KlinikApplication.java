package com.example.klinik;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.example.Pasien;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class KlinikApplication extends JFrame {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField nameField, nikField, dobField, addressField;
    private JButton createButton, editButton, deleteButton, closeButton;
    private Connection connection;

    public KlinikApplication() {
        try {
            String url = "jdbc:mysql://localhost:3306/binus-bad";
            String username = "root";
            String password = "mysecretpassword";

            connection = DriverManager.getConnection(url, username, password);

            System.out.println("==> Database connected!");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        setTitle("Klinik Melati");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.setColumnIdentifiers(new Object[] { "No", "Nama Pasien", "NIK", "Tanggal Lahir", "Alamat" });

        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(6, 2));
        inputPanel.add(new JLabel("Nama Pasien:"));
        nameField = new JTextField();
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("NIK:"));
        nikField = new JTextField();
        inputPanel.add(nikField);
        inputPanel.add(new JLabel("Tanggal Lahir (yyyy/mm/dd):"));
        dobField = new JTextField();
        inputPanel.add(dobField);
        inputPanel.add(new JLabel("Alamat:"));
        addressField = new JTextField();
        inputPanel.add(addressField);

        createButton = new JButton("Create");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });
        inputPanel.add(createButton);

        editButton = new JButton("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editPatient();
            }
        });
        inputPanel.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePatient();
            }
        });
        inputPanel.add(deleteButton);

        closeButton = new JButton("Close");
        closeButton.setEnabled(false);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetFormState();
            }
        });
        inputPanel.add(closeButton);

        add(inputPanel, BorderLayout.SOUTH);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow != -1) {
                    editButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    closeButton.setEnabled(true);
                    createButton.setEnabled(false);

                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd");
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(table.getValueAt(selectedRow, 3).toString(), inputFormatter);
                    String formattedDate = date.format(outputFormatter);

                    nameField.setText(table.getValueAt(selectedRow, 1).toString());
                    nikField.setText(table.getValueAt(selectedRow, 2).toString());
                    dobField.setText(formattedDate);
                    addressField.setText(table.getValueAt(selectedRow, 4).toString());
                } else {
                    editButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    closeButton.setEnabled(false);
                    createButton.setEnabled(true);
                }
            }
        });

        initPatientsData();
    }

    private boolean validateDate(String dateString) {
        try {
            LocalDate.parse(dateString);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void initPatientsData() {
        // reset row
        tableModel.setRowCount(0);

        List<Pasien> patients = readAll();

        patients.forEach((p) -> {
            addPatientData(p.getName(), p.getNik().toString(), p.getDate(), p.getAddress());
        });
    }

    private void addPatientData(String name, String nik, String dob, String address) {
        Vector<Object> row = new Vector<>();

        LocalDate date = LocalDate.parse(dob);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd");
        String formattedDate = date.format(formatter);

        row.add(tableModel.getRowCount() + 1);
        row.add(name);
        row.add(nik);
        row.add(formattedDate);
        row.add(address);
        tableModel.addRow(row);
    }

    private void addPatient() {
        String name = nameField.getText();
        String nik = nikField.getText();
        String dob = dobField.getText();
        String address = addressField.getText();

        if (name.isEmpty() || nik.isEmpty() || dob.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan isi semua form.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validateDate(dob)) {
            JOptionPane.showMessageDialog(this, "Form tanggal lahir tidak valid.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pasien pasien = new Pasien();

        pasien.setName(name);
        pasien.setNik(Long.parseLong(nik));
        pasien.setDate(dob);
        pasien.setAddress(address);

        create(pasien);
        initPatientsData();
        resetFormState();
    }

    private void editPatient() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String name = nameField.getText();
            String nik = nikField.getText();
            String dob = dobField.getText();
            String address = addressField.getText();

            // Validate input fields
            if (name.isEmpty() || nik.isEmpty() || dob.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Silahkan isi semua form.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!validateDate(dob)) {
                JOptionPane.showMessageDialog(this, "Form tanggal lahir tidak valid.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Pasien pasien = new Pasien();

            pasien.setName(name);
            pasien.setNik(Long.parseLong(nik));
            pasien.setDate(dob);
            pasien.setAddress(address);

            update(pasien);
            initPatientsData();
            resetFormState();
        }
    }

    private void deletePatient() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow != -1) {
            delete(Long.parseLong(nikField.getText()));
            resetFormState();
            initPatientsData();
        }
    }

    private void resetFormState() {
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        closeButton.setEnabled(false);
        createButton.setEnabled(true);

        nameField.setText("");
        nikField.setText("");
        dobField.setText("");
        addressField.setText("");

        table.clearSelection();
    }

    public void create(Pasien data) {
        String sql = "INSERT INTO patients (name, nik, date, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getName());
            statement.setLong(2, data.getNik());
            statement.setString(3, data.getDate());
            statement.setString(4, data.getAddress());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Pasien> readAll() {
        List<Pasien> dataList = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Pasien data = new Pasien();
                data.setId(resultSet.getInt("id"));
                data.setName(resultSet.getString("name"));
                data.setNik(resultSet.getLong("nik"));
                data.setDate(resultSet.getString("date"));
                data.setAddress(resultSet.getString("address"));
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dataList;
    }

    public void update(Pasien data) {
        String sql = "UPDATE patients SET name = ?, nik = ?, date = ?, address = ? WHERE nik = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, data.getName());
            statement.setLong(2, data.getNik());
            statement.setString(3, data.getDate());
            statement.setString(4, data.getAddress());
            statement.setLong(5, data.getNik());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(Long nik) {
        String sql = "DELETE FROM patients WHERE nik = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, nik);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new KlinikApplication().setVisible(true);
            }
        });
    }
}