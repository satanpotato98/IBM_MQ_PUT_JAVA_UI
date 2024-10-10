package com.example;

import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQPutMessageOptions;
import com.ibm.mq.MQQueue;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;

public class MQFileSenderGUI extends JFrame {

    private JTextField filePathField;
    private JTextField hostField;
    private JTextField portField;
    private JTextField queueManagerField;
    private JTextField channelField;
    private JTextField queueNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton sendButton;
    private JTextArea outputArea;

    public MQFileSenderGUI() {
        setTitle("MQ File Sender");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // Create labels and fields
        JLabel filePathLabel = new JLabel("File Path:");
        filePathField = new JTextField();

        JLabel hostLabel = new JLabel("Host:");
        hostField = new JTextField();

        JLabel portLabel = new JLabel("Port:");
        portField = new JTextField("1414");

        JLabel queueManagerLabel = new JLabel("Queue Manager:");
        queueManagerField = new JTextField();

        JLabel channelLabel = new JLabel("Channel:");
        channelField = new JTextField();

        JLabel queueNameLabel = new JLabel("Queue Name:");
        queueNameField = new JTextField();

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        sendButton = new JButton("Send Message");
        sendButton.addActionListener(this::sendMessage);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);

        // Layout setup
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5); // Padding

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(filePathLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        panel.add(filePathField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(hostLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hostField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(portLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(queueManagerLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(queueManagerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(channelLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(channelField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(queueNameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(queueNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(sendButton, gbc);

        // Add panel and output area to the frame
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(outputArea), BorderLayout.CENTER);
    }

    private void sendMessage(ActionEvent event) {
        String filePath = filePathField.getText().trim();
        String HOST = hostField.getText().trim();
        String portText = portField.getText().trim();
        String QMGR = queueManagerField.getText().trim();
        String CHANNEL = channelField.getText().trim();
        String QUEUE_NAME = queueNameField.getText().trim();
        String USERNAME = usernameField.getText().trim();
        String PASSWORD = new String(passwordField.getPassword());

        // Input validation
        if (filePath.isEmpty() || HOST.isEmpty() || portText.isEmpty() || QMGR.isEmpty() ||
                CHANNEL.isEmpty() || QUEUE_NAME.isEmpty() || USERNAME.isEmpty()) {
            outputArea.setText("Please fill in all fields.");
            return;
        }

        int PORT;
        try {
            PORT = Integer.parseInt(portText);
        } catch (NumberFormatException e) {
            outputArea.setText("Invalid port number.");
            return;
        }

        try {
            // Read file content
            String fileContent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

            // Set MQ connection properties
            Hashtable<String, Object> props = new Hashtable<>();
            props.put(CMQC.HOST_NAME_PROPERTY, HOST);
            props.put(CMQC.PORT_PROPERTY, PORT);
            props.put(CMQC.CHANNEL_PROPERTY, CHANNEL);
            props.put(CMQC.TRANSPORT_PROPERTY, CMQC.TRANSPORT_MQSERIES_CLIENT);

            // Include credentials
            props.put(CMQC.USER_ID_PROPERTY, USERNAME);
            props.put(CMQC.PASSWORD_PROPERTY, PASSWORD);

            // Create MQQueueManager
            MQQueueManager qMgr = new MQQueueManager(QMGR, props);

            // Open the queue for output
            int openOptions = CMQC.MQOO_OUTPUT | CMQC.MQOO_FAIL_IF_QUIESCING;
            MQQueue queue = qMgr.accessQueue(QUEUE_NAME, openOptions);

            // Create a message
            MQMessage msg = new MQMessage();
            msg.format = CMQC.MQFMT_STRING;
            msg.characterSet = 1208; // UTF-8 character set
            msg.writeString(fileContent);

            // Specify message options
            MQPutMessageOptions pmo = new MQPutMessageOptions();

            // Put the message on the queue
            queue.put(msg, pmo);

            outputArea.setText("Message sent successfully to queue: " + QUEUE_NAME);

            // Close the queue and disconnect
            queue.close();
            qMgr.disconnect();

        } catch (IOException e) {
            outputArea.setText("IOException occurred while reading the file:\n" + e.getMessage());
            e.printStackTrace();
        } catch (MQException e) {
            outputArea.setText("An IBM MQ error occurred: Completion code " + e.completionCode + ", Reason code " + e.reasonCode);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MQFileSenderGUI app = new MQFileSenderGUI();
            app.setVisible(true);
        });
    }
}
