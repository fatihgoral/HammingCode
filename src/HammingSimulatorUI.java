import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HammingSimulatorUI extends JFrame {
    private JTextField dataInputField;
    private JComboBox<String> lengthComboBox;
    private JPanel resultPanel;
    private int[] originalData;
    private int[] hammingCode;
    private int[] corruptedCode;
    private int[] errorPositions = new int[2];
    private int errorCount = 0;
    private boolean isCorrected = false;

    public HammingSimulatorUI() {
        setTitle("Hamming Code Generator");
        setSize(1280, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(30, 30, 33));

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBackground(new Color(34, 40, 49));

        // Program title (Hamming Code Generator)
        JLabel titleLabel = new JLabel("Hamming Code Generator", JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(220, 220, 220));
        titleLabel.setBounds(20, 30, 280, 50);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(53, 60, 72));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftPanel.add(titleLabel);

        // "Data" label
        JLabel dataLabel = new JLabel("Data:");
        dataLabel.setForeground(new Color(200, 200, 200)); // More readable color
        dataLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Smaller font
        dataLabel.setBounds(20, 100, 80, 30);
        leftPanel.add(dataLabel);

        // Data input field
        dataInputField = new JTextField(20);
        dataInputField.setBounds(70, 100, 300, 40);
        dataInputField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        dataInputField.setBackground(new Color(240, 240, 240));
        dataInputField.setForeground(Color.BLACK);
        dataInputField.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true)); // Light border
        leftPanel.add(dataInputField);

        // "Length" label
        JLabel lengthLabel = new JLabel("Length:");
        lengthLabel.setForeground(new Color(200, 200, 200));
        lengthLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lengthLabel.setBounds(20, 160, 100, 30);
        leftPanel.add(lengthLabel);

        // Length selection (JComboBox)
        lengthComboBox = new JComboBox<>(new String[]{"8", "16", "32"});
        lengthComboBox.setBounds(120, 160, 80, 40);
        lengthComboBox.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lengthComboBox.setBackground(new Color(240, 240, 240));
        lengthComboBox.setForeground(Color.BLACK); // Black text color
        lengthComboBox.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1, true)); // Light border
        leftPanel.add(lengthComboBox);

        // Encode button
        JButton encodeButton = createStyledButton(" Encode ", new Color(46, 204, 113));
        encodeButton.setBounds(20, 220, 280, 50);
        leftPanel.add(encodeButton);

        // Inject Error button
        JButton errorButton = createEnhancedButton(" Inject Error ", new Color(241, 106, 15), new Color(255, 215, 64));
        errorButton.setBounds(20, 290, 280, 50);
        leftPanel.add(errorButton);

        // Correct Error button
        JButton correctButton = createStyledButton(" Correct Error ", new Color(52, 152, 219));
        correctButton.setBounds(20, 360, 280, 50);
        leftPanel.add(correctButton);

        resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBackground(new Color(200, 255, 200));

        JScrollPane scrollPane = new JScrollPane(resultPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(200, 255, 200));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, scrollPane);
        splitPane.setDividerLocation(0.25);
        splitPane.setResizeWeight(0.25);
        splitPane.setDividerSize(5);
        add(splitPane);

        encodeButton.addActionListener(e -> {
            resultPanel.removeAll();
            String input = dataInputField.getText().trim();
            int expectedLength = Integer.parseInt((String) lengthComboBox.getSelectedItem());
            if (input.isEmpty()) {
                showStyledMessageDialog("Error", "Data input is required.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!input.matches("[01]+")) {
                showStyledMessageDialog("Error", "Only 0s and 1s are allowed.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (input.length() != expectedLength) {
                showStyledMessageDialog("Error", "Data must be " + expectedLength + " bits long.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            originalData = input.chars().map(c -> c - '0').toArray();
            hammingCode = encodeHamming(originalData);
            corruptedCode = null;
            errorCount = 0;
            errorPositions[0] = -1;
            errorPositions[1] = -1;
            isCorrected = false; // Reset correction flag
            drawBitRow("Hamming Code", hammingCode, -1);
            resultPanel.revalidate();
            resultPanel.repaint();
        });

        errorButton.addActionListener(e -> {
            if (hammingCode == null) return;
            if (isCorrected) {
                showStyledMessageDialog("Error", "The error has already been corrected. Please encode new data to inject a new error.", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (errorCount >= 2) return; // Allow a maximum of 2 errors
            showBitPositionDialog(hammingCode.length);
        });

        correctButton.addActionListener(e -> {
            if (corruptedCode == null) return;
            if (isCorrected) {
                showStyledMessageDialog("Warning", "You have already corrected it.", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (errorCount == 2) {
                int pos1 = errorPositions[0]; // Kullanıcının girdiği pozisyonu direkt kullan
                int pos2 = errorPositions[1]; // Kullanıcının girdiği pozisyonu direkt kullan
                if (pos1 == pos2) {
                    showStyledMessageDialog("Result", "No error detected, the data is correct.", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showStyledMessageDialog("Error", "Double parity error detected, cannot be corrected. Positions: " + pos1 + ", " + pos2, JOptionPane.ERROR_MESSAGE);
                }
                isCorrected = true; // Set correction flag
                errorCount = 0; // Reset error count
                errorPositions[0] = -1;
                errorPositions[1] = -1;
                return;
            }
            int errorPos = detectError(corruptedCode);
            if (errorPos > 0) {
                errorPos = hammingCode.length - errorPos; // Convert to right-based 0-index
                corruptedCode[errorPos] ^= 1; // Correct the error
            }
            drawBitRow("Corrected Code", corruptedCode, -1);
            isCorrected = true; // Set correction flag
            errorCount = 0; // Reset error count
            errorPositions[0] = -1;
            errorPositions[1] = -1;
            resultPanel.revalidate();
            resultPanel.repaint();
        });
    }

    private void drawBitRow(String title, int[] bits, int highlightIndex) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(new Color(200, 255, 200));
        JLabel titleLabel = new JLabel(title + ":  ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 153));
        row.add(titleLabel);
        for (int i = 0; i < bits.length; i++) {
            JLabel bit = new JLabel(Integer.toString(bits[i]));
            bit.setOpaque(true);
            bit.setFont(new Font("Consolas", Font.BOLD, 20));
            bit.setHorizontalAlignment(JLabel.CENTER);
            bit.setPreferredSize(new Dimension(20, 30));
            bit.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
            int position = bits.length - i;
            boolean isParityBit = isPowerOfTwo(position);
            if (i == highlightIndex) {
                bit.setBackground(Color.RED);
                bit.setForeground(Color.WHITE);
            } else if (isParityBit && (title.equals("Hamming Code"))) {
                bit.setBackground(new Color(0, 0, 255));
                bit.setForeground(Color.WHITE);
            } else {
                bit.setBackground(new Color(245, 255, 250));
                bit.setForeground(Color.BLACK);
            }
            row.add(bit);
        }
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(200, 255, 200));
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 20, 10, 20),
                BorderFactory.createLineBorder(new Color(180, 180, 220), 2)
        ));
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        resultPanel.add(Box.createVerticalStrut(8));
        resultPanel.add(wrapper);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setOpaque(true);
        return button;
    }

    private JButton createEnhancedButton(String text, Color startColor, Color endColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, startColor, 0, getHeight(), endColor);
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 64, 100), 2, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        return button;
    }

    private void showStyledMessageDialog(String title, String message, int messageType) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(44, 47, 51));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 0, 0), 2, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>", JLabel.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        messageLabel.setForeground(Color.WHITE);
        panel.add(messageLabel, BorderLayout.CENTER);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        okButton.setBackground(new Color(200, 0, 0));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        okButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(44, 47, 51));
        buttonPanel.add(okButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void showBitPositionDialog(int maxLength) {
        JDialog dialog = new JDialog(this, "Input", true);
        dialog.setUndecorated(true);
        dialog.setSize(350, 200); // Reduced dialog size
        dialog.setLocationRelativeTo(this);

        // Panel with gradient background (light blue to slightly darker blue)
        JPanel panel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(173, 216, 230), 0, getHeight(), new Color(135, 206, 250));
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(147, 112, 219), 3, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel iconLabel = new JLabel("?");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        iconLabel.setForeground(new Color(138, 43, 226));
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel("Enter bit position (1 to " + maxLength + "):");
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        messageLabel.setForeground(new Color(138, 43, 226));
        panel.add(messageLabel, BorderLayout.NORTH);


        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputPanel.setOpaque(false);
        JTextField inputField = new JTextField();
        inputField.setFont(new Font("Consolas", Font.PLAIN, 18));
        inputField.setPreferredSize(new Dimension(150, 30));
        inputField.setBorder(BorderFactory.createLineBorder(new Color(147, 112, 219), 2));
        inputPanel.add(inputField);
        panel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton okButton = new JButton("OK") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(255, 165, 0), 0, getHeight(), new Color(255, 140, 0)); // Orange gradient
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        okButton.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Larger font
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25)); // Larger padding
        okButton.setContentAreaFilled(false);
        okButton.addActionListener(e -> {
            String posInput = inputField.getText().trim();
            if (posInput.isEmpty() || !posInput.matches("\\d+") || Integer.parseInt(posInput) < 1 || Integer.parseInt(posInput) > maxLength) return;
            int bitNo = Integer.parseInt(posInput);
            if (errorCount == 0) {
                corruptedCode = hammingCode.clone();
                int pos = hammingCode.length - bitNo;
                errorPositions[errorCount] = bitNo;
                errorCount++;
                corruptedCode[pos] ^= 1;
                drawBitRow("Corrupted Code", corruptedCode, pos);
                addStyledErrorMessage("Error injected at bit position: " + bitNo);
            } else {
                int pos = hammingCode.length - bitNo;
                errorPositions[errorCount] = bitNo;
                errorCount++;
                corruptedCode[pos] ^= 1;
                drawBitRow("Corrupted Code", corruptedCode, pos);
                addStyledErrorMessage("Error injected at bit position: " + bitNo);
            }
            resultPanel.revalidate();
            resultPanel.repaint();
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Cancel") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(0, 0, new Color(211, 211, 211), 0, getHeight(), new Color(169, 169, 169)); // Gray gradient
                g2.setPaint(gradient);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g2);
                g2.dispose();
            }
        };
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Larger font
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25)); // Larger padding
        cancelButton.setContentAreaFilled(false); // Needed for gradient
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void addStyledErrorMessage(String message) {
        JPanel errorPanel = new JPanel(new BorderLayout(10, 5));
        errorPanel.setBackground(new Color(255, 235, 235));
        errorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 0, 0), 1, true),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        errorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        iconLabel.setForeground(new Color(200, 0, 0));
        errorPanel.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        messageLabel.setForeground(Color.BLACK);
        errorPanel.add(messageLabel, BorderLayout.CENTER);

        resultPanel.add(Box.createVerticalStrut(8));
        resultPanel.add(errorPanel);
    }

    private int[] encodeHamming(int[] data) {
        int r = 0;
        while ((1 << r) < data.length + r + 1) r++;
        int[] result = new int[data.length + r];
        int j = 0;
        for (int i = 1; i <= result.length; i++) {
            if ((i & (i - 1)) != 0) {
                result[result.length - i] = data[data.length - 1 - j++];
            }
        }
        for (int i = 0; i < r; i++) {
            int pos = 1 << i;
            int parity = 0;
            for (int k = 1; k <= result.length; k++) {
                if ((k & pos) != 0) {
                    parity ^= result[result.length - k];
                }
            }
            result[result.length - pos] = parity;
        }
        return result;
    }

    private int detectError(int[] code) {
        int r = 0;
        while ((1 << r) < code.length) r++;
        int syndrome = 0;
        for (int i = 0; i < r; i++) {
            int parity = 0;
            for (int j = 0; j < code.length; j++) {
                int pos = code.length - j;
                if ((pos & (1 << i)) != 0) {
                    parity ^= code[j];
                }
            }
            if (parity != 0) syndrome |= (1 << i);
        }
        return syndrome == 0 ? 0 : syndrome;
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HammingSimulatorUI sim = new HammingSimulatorUI();
            sim.setVisible(true);
        });
    }
}