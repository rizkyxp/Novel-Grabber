import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.Objects;

/*
 *  Window display and handling.
 */
public class NovelGrabberGUI {
    static final DefaultListModel<String> listModelChapterLinks = new DefaultListModel<>();
    private static final String NL = System.getProperty("line.separator");
    private static final String[] fileTypeList = {".html", ".txt"};
    private static final JList<String> chapterLinkList = new JList<>(listModelChapterLinks);
    static JTextField singleChapterLink;
    static JTextField chapterListURL;
    static JTextField saveLocation;
    static JComboBox<String> allChapterHostSelection;
    static JComboBox<String> singleChapterHostSelection;
    static JComboBox<String> fileType;
    static JTextField firstChapter;
    static JTextField lastChapter;
    static JTextField waitTime;
    static JCheckBox useSentenceSelector;
    static JCheckBox useNumeration;
    static JCheckBox checkInvertOrder;
    static JCheckBox chapterAllCheckBox;
    static JCheckBox manUseSentenceSelector;
    static JTextField manSaveLocation;
    static JTextField manWaitTime;
    static JComboBox manFileType;
    static JTextField manChapterContainer;
    static JTextField manSentenceSelector;
    static JCheckBox manCheckInvertOrder;
    static JCheckBox manUseNumeration = new JCheckBox("Chapter numeration");
    static JTextField manChapterListURL;
    private static JTextArea logArea;
    private static JTextArea manLogField;
    private static JProgressBar progressBar;
    private static JProgressBar manProgressBar;
    private static JCheckBox manCreateToc = new JCheckBox("Create ToC");
    private JFrame frmNovelGrabber;

    /**
     * Create the application.
     */
    private NovelGrabberGUI() {
        initialize();
    }

    /**
     * Launch the application
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                NovelGrabberGUI window = new NovelGrabberGUI();
                window.frmNovelGrabber.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static void appendText(String logWindow, String logMsg) {
        switch (logWindow) {
            case "auto":
                logArea.append(logMsg + NL);
                logArea.update(logArea.getGraphics());
                logArea.setCaretPosition(logArea.getText().length());
                break;
            case "manual":
                manLogField.append(logMsg + NL);
                manLogField.setCaretPosition(manLogField.getText().length());
                break;
        }
    }

    static void updateProgress(String progressBarSelect) {
        switch (progressBarSelect) {
            case "auto":
                progressBar.setValue(progressBar.getValue() + 1);
                if (progressBar.getValue() < progressBar.getMaximum()) {
                    progressBar.setString((progressBar.getValue() + 1) + " / " + progressBar.getMaximum());
                }
                progressBar.update(progressBar.getGraphics());
                break;
            case "manual":
                manProgressBar.setValue(manProgressBar.getValue() + 1);
                if (manProgressBar.getValue() < manProgressBar.getMaximum()) {
                    manProgressBar.setString((manProgressBar.getValue() + 1) + " / " + manProgressBar.getMaximum());
                }
                manProgressBar.update(manProgressBar.getGraphics());
                break;
        }
    }

    static void setMaxProgress(String progressBarSelect, int i) {
        switch (progressBarSelect) {
            case "auto":
                progressBar.setMaximum(i);
                progressBar.setString("0 / " + i);
                progressBar.update(progressBar.getGraphics());
                break;
            case "manual":
                manProgressBar.setMaximum(i);
                manProgressBar.setString("0 / " + i);
                manProgressBar.update(manProgressBar.getGraphics());
                break;
        }
    }

    private static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void addPopup(Component component, final JPopupMenu popup) {
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showMenu(e);
                }
            }

            private void showMenu(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    /**
     * Initialize the contents of the frame.
     */

    private void initialize() {
        // tooltip style
        int dismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
        dismissDelay = Integer.MAX_VALUE;
        ToolTipManager.sharedInstance().setDismissDelay(dismissDelay);
        UIManager.put("ToolTip.background", new ColorUIResource(Color.white));
        String toolTipStyle = "<html><p width=\"300\">";

        frmNovelGrabber = new JFrame();
        frmNovelGrabber.setResizable(false);
        String versionNumber = "v1.3.0";
        frmNovelGrabber.setTitle("Novel-Grabber " + versionNumber);
        frmNovelGrabber.setBounds(100, 100, 588, 650);
        frmNovelGrabber.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmNovelGrabber.getContentPane().setLayout(null);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFocusable(false);
        tabbedPane.setBounds(0, 0, 594, 634);
        frmNovelGrabber.getContentPane().add(tabbedPane);

        JPanel automaticPane = new JPanel();
        automaticPane.setLayout(null);
        tabbedPane.addTab("Automatic", null, automaticPane, null);

        JPanel allChapterPane = new JPanel();
        allChapterPane.setBounds(10, 5, 557, 473);
        automaticPane.add(allChapterPane);
        allChapterPane.setBorder(BorderFactory.createTitledBorder("Get multiple chapters"));
        allChapterPane.setLayout(null);

        progressBar = new JProgressBar();
        progressBar.setBounds(10, 436, 409, 25);
        allChapterPane.add(progressBar);
        progressBar.setFont(new Font("Tahoma", Font.PLAIN, 15));
        progressBar.setForeground(new Color(0, 128, 128));
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setString("");

        JButton grabChapters = new JButton("Grab chapters");
        grabChapters.setFocusPainted(false);
        grabChapters.setFont(new Font("Tahoma", Font.PLAIN, 11));
        grabChapters.setBounds(429, 435, 113, 26);
        allChapterPane.add(grabChapters);

        chapterListURL = new JTextField();
        chapterListURL.setBounds(152, 19, 390, 25);
        allChapterPane.add(chapterListURL);
        chapterListURL.setToolTipText("");
        chapterListURL.setColumns(10);

        JLabel lblNovelChapterList = new JLabel("Table of Contents URL:");
        lblNovelChapterList.setLabelFor(chapterListURL);
        lblNovelChapterList.setBounds(10, 19, 116, 25);
        allChapterPane.add(lblNovelChapterList);
        lblNovelChapterList.setFont(new Font("Tahoma", Font.PLAIN, 11));

        JLabel lblDestinationDirectory = new JLabel("Save directory:");
        lblDestinationDirectory.setBounds(10, 80, 103, 25);
        allChapterPane.add(lblDestinationDirectory);
        lblDestinationDirectory.setFont(new Font("Tahoma", Font.PLAIN, 11));

        saveLocation = new JTextField();
        saveLocation.setBounds(152, 80, 294, 25);
        allChapterPane.add(saveLocation);
        saveLocation.setToolTipText("");
        saveLocation.setColumns(10);

        allChapterHostSelection = new JComboBox<>(Novel.websites);
        allChapterHostSelection.setFocusable(false);
        allChapterHostSelection.setBounds(152, 49, 294, 25);
        allChapterPane.add(allChapterHostSelection);

        JLabel lblNewLabel = new JLabel("Host website:");
        lblNewLabel.setBounds(10, 49, 86, 25);
        allChapterPane.add(lblNewLabel);
        lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));

        JButton btnNewButton = new JButton("Browse...");
        btnNewButton.setFocusPainted(false);
        btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnNewButton.addActionListener(arg0 -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Choose destination directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                saveLocation.setText(chooser.getSelectedFile().toString());
            }
        });
        btnNewButton.setBounds(456, 79, 86, 27);
        allChapterPane.add(btnNewButton);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setBounds(-22, 11, 235, 41);
        allChapterPane.add(logArea);

        JScrollPane scrollPane = new JScrollPane(logArea);

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        addPopup(logArea, popupMenu);

        JMenuItem saveLogBtn = new JMenuItem("Save log to file");
        saveLogBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        saveLogBtn.addActionListener(arg0 -> {
            if (!logArea.getText().isEmpty()) {
                String fileName = "log.txt";
                try (PrintStream out = new PrintStream(saveLocation.getText() + File.separator + fileName,
                        "UTF-8")) {
                    out.print(logArea.getText());
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else {
                showPopup("Log is empty", "warning");
            }

        });

        popupMenu.add(saveLogBtn);

        JMenuItem mntmClearLog = new JMenuItem("Clear log");
        mntmClearLog.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        mntmClearLog.addActionListener(e -> {
            if (!logArea.getText().isEmpty()) {
                logArea.setText(null);
            }
        });

        JSeparator separator_1 = new JSeparator();
        popupMenu.add(separator_1);
        popupMenu.add(mntmClearLog);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(10, 267, 532, 163);
        allChapterPane.add(scrollPane);

        JPanel chapterSelect = new JPanel();
        chapterSelect.setBounds(10, 109, 532, 45);
        chapterSelect.setBorder(BorderFactory.createTitledBorder("Select chapters to download"));
        allChapterPane.add(chapterSelect);
        chapterSelect.setLayout(null);

        chapterAllCheckBox = new JCheckBox("All");
        chapterAllCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 11));
        chapterAllCheckBox.setFocusable(false);
        chapterAllCheckBox.setBounds(101, 12, 62, 23);
        chapterSelect.add(chapterAllCheckBox);
        chapterAllCheckBox.addItemListener(arg0 -> {
            if (chapterAllCheckBox.isSelected()) {
                firstChapter.setEnabled(false);
                lastChapter.setEnabled(false);
            }
            if (!chapterAllCheckBox.isSelected()) {
                firstChapter.setEnabled(true);
                lastChapter.setEnabled(true);
            }
        });

        JLabel lblChapter = new JLabel("Chapter range:");
        lblChapter.setBounds(210, 16, 113, 14);
        chapterSelect.add(lblChapter);

        firstChapter = new JTextField();
        firstChapter.setBounds(325, 13, 60, 20);
        firstChapter.setColumns(10);
        firstChapter.setHorizontalAlignment(JTextField.CENTER);
        chapterSelect.add(firstChapter);

        JLabel lblTo = new JLabel("-");
        lblTo.setBounds(396, 11, 6, 20);
        lblTo.setFont(new Font("Tahoma", Font.PLAIN, 16));
        chapterSelect.add(lblTo);

        lastChapter = new JTextField();
        lastChapter.setBounds(413, 13, 60, 20);
        lastChapter.setColumns(10);
        lastChapter.setHorizontalAlignment(JTextField.CENTER);
        chapterSelect.add(lastChapter);

        JPanel optionSelect = new JPanel();
        optionSelect.setBounds(10, 165, 532, 95);
        optionSelect.setBorder(BorderFactory.createTitledBorder("Option select"));
        allChapterPane.add(optionSelect);
        optionSelect.setLayout(null);

        JCheckBox createTocCheckBox = new JCheckBox("Create ToC");
        createTocCheckBox.setFocusPainted(false);
        createTocCheckBox.setFont(new Font("Tahoma", Font.PLAIN, 11));
        createTocCheckBox.setBounds(6, 20, 81, 23);
        optionSelect.add(createTocCheckBox);
        createTocCheckBox.setToolTipText(toolTipStyle
                + "Will create a \"Table of Contents\" file which can be used to convert all chapter files into a single epub file in calibre.</p></html>");

        fileType = new JComboBox<>(fileTypeList);
        fileType.setFocusable(false);
        fileType.setFont(new Font("Tahoma", Font.PLAIN, 11));
        fileType.setBounds(456, 22, 66, 20);
        optionSelect.add(fileType);

        JLabel fileTypeLabel = new JLabel("File output:");
        fileTypeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));
        fileTypeLabel.setBounds(389, 20, 66, 25);
        optionSelect.add(fileTypeLabel);

        useNumeration = new JCheckBox("Chapter numeration");
        useNumeration.setFocusPainted(false);
        useNumeration.setToolTipText(toolTipStyle
                + "Will add a chapter number infront of the chapter name. Helpful for ordering chapters which don't have a chapter number in their title.</p></html>");
        useNumeration.setFont(new Font("Tahoma", Font.PLAIN, 11));
        useNumeration.setBounds(6, 40, 121, 23);
        optionSelect.add(useNumeration);

        checkInvertOrder = new JCheckBox("Invert chapter order");
        checkInvertOrder.setFocusPainted(false);
        checkInvertOrder.setToolTipText(
                "<html><p width=\"300\">Invert  the chapter order and download the last chapter first. Useful if sites list the highest chapter at the top</p></html>");
        checkInvertOrder.setFont(new Font("Tahoma", Font.PLAIN, 11));
        checkInvertOrder.setBounds(6, 60, 129, 23);
        optionSelect.add(checkInvertOrder);

        useSentenceSelector = new JCheckBox("Ignore sentence selector");
        useSentenceSelector.setFocusPainted(false);
        useSentenceSelector.setToolTipText(
                "<html><p width=\"300\">Grabs all text within the chapter container. Useful if chapters use a spreadsheat to display various things such as character stats in a VRMMO novel for example. "
                        + "Also required for some sites/chapters which do not embed the text in paragraph tags.</p></html>");
        useSentenceSelector.setFont(new Font("Tahoma", Font.PLAIN, 11));
        useSentenceSelector.setBounds(151, 20, 150, 23);
        optionSelect.add(useSentenceSelector);

        JLabel sleepLbl = new JLabel("Wait time:");
        sleepLbl.setBounds(389, 60, 66, 14);
        sleepLbl.setToolTipText(
                "<html><p width=\"300\">Time in miliseconds to wait before each chapter grab. (1000 for 1 second).</p></html>");
        optionSelect.add(sleepLbl);

        waitTime = new JTextField();
        waitTime.setHorizontalAlignment(SwingConstants.CENTER);
        waitTime.setColumns(10);
        waitTime.setBounds(456, 57, 66, 20);
        waitTime.setText("0");
        optionSelect.add(waitTime);

        JButton btnVisitWebsite = new JButton("Visit...");
        btnVisitWebsite.addActionListener(arg0 -> {
            try {
                Novel emptyNovel = new Novel(
                        Objects.requireNonNull(allChapterHostSelection.getSelectedItem()).toString().toLowerCase().replace(" ", ""), "");
                URI uri = new URI(emptyNovel.getHost());
                openWebpage(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
        btnVisitWebsite.setFocusPainted(false);
        btnVisitWebsite.setBounds(456, 48, 86, 27);
        allChapterPane.add(btnVisitWebsite);

        //Single chapter
        JPanel singleChapterPane = new JPanel();
        singleChapterPane.setBounds(10, 489, 557, 95);
        automaticPane.add(singleChapterPane);
        singleChapterPane.setBorder(BorderFactory.createTitledBorder("Get single chapter"));
        singleChapterPane.setLayout(null);

        JButton getChapterBtn = new JButton("Grab chapter");
        getChapterBtn.setFocusPainted(false);
        getChapterBtn.setFont(new Font("Tahoma", Font.PLAIN, 11));
        getChapterBtn.setBounds(429, 58, 113, 27);
        singleChapterPane.add(getChapterBtn);

        singleChapterLink = new JTextField();
        singleChapterLink.setBounds(133, 25, 409, 25);
        singleChapterPane.add(singleChapterLink);
        singleChapterLink.setColumns(10);

        JLabel lblchapterURL = new JLabel("Chapter URL:");
        lblchapterURL.setBounds(10, 24, 100, 25);
        singleChapterPane.add(lblchapterURL);
        lblchapterURL.setFont(new Font("Tahoma", Font.PLAIN, 11));

        JLabel label = new JLabel("Host website:");
        label.setFont(new Font("Tahoma", Font.PLAIN, 11));
        label.setBounds(10, 59, 73, 25);
        singleChapterPane.add(label);

        singleChapterHostSelection = new JComboBox<>(Novel.websites);
        singleChapterHostSelection.setFocusable(false);
        singleChapterHostSelection.setBounds(133, 59, 286, 25);
        singleChapterPane.add(singleChapterHostSelection);

        // Manual Tab
        JPanel manualPane = new JPanel();
        tabbedPane.addTab("Manual", null, manualPane, null);
        manualPane.setLayout(null);

        JPanel chapterLinkPane = new JPanel();
        chapterLinkPane.setBounds(10, 5, 557, 298);
        manualPane.add(chapterLinkPane);
        chapterLinkPane.setBorder(BorderFactory.createTitledBorder("Chapter links select"));
        chapterLinkPane.setLayout(null);

        JLabel lblManualToc = new JLabel("Table Of Contents URL");
        lblManualToc.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblManualToc.setBounds(10, 17, 158, 25);
        chapterLinkPane.add(lblManualToc);

        manChapterListURL = new JTextField();
        manChapterListURL.setBounds(178, 14, 260, 25);
        chapterLinkPane.add(manChapterListURL);
        manChapterListURL.setColumns(10);

        JButton removeLinks = new JButton("Remove links");
        removeLinks.setFont(new Font("Tahoma", Font.PLAIN, 11));
        removeLinks.setEnabled(false);
        removeLinks.setFocusPainted(false);
        removeLinks.addActionListener(arg0 -> {
            int[] indices = chapterLinkList.getSelectedIndices();
            for (int i = indices.length - 1; i >= 0; i--) {
                listModelChapterLinks.removeElementAt(indices[i]);
                fetchChapters.chapterURLs.remove(indices[i]);
            }
            appendText("manual", indices.length + " links removed.");
        });
        removeLinks.setBounds(448, 55, 99, 25);
        chapterLinkPane.add(removeLinks);

        JTabbedPane linkSelectTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        linkSelectTabbedPane.setFocusable(false);
        linkSelectTabbedPane.setBounds(10, 86, 537, 207);

        chapterLinkPane.add(linkSelectTabbedPane);

        JPanel manLinkSelect = new JPanel();
        manLinkSelect.setBackground(Color.WHITE);
        manLinkSelect.setLayout(null);
        linkSelectTabbedPane.addTab("Link select", null, manLinkSelect, null);

        chapterLinkList.setBackground(Color.WHITE);
        chapterLinkList.setVisibleRowCount(-1);
        chapterLinkList.setLayoutOrientation(JList.VERTICAL_WRAP);
        chapterLinkList.setFixedCellWidth(268);
        chapterLinkPane.add(chapterLinkList);

        manLogField = new JTextArea();
        manLogField.setFocusable(false);
        manLogField.setBounds(0, 0, 532, 159);
        manLogField.setEditable(false);

        JPanel manLogArea = new JPanel();
        manLogArea.add(manLogField);
        manLogArea.setLayout(null);
        linkSelectTabbedPane.addTab("Log", null, manLogArea, null);

        JScrollPane scrollPane_1 = new JScrollPane(chapterLinkList);
        scrollPane_1.setBounds(0, 0, 532, 179);
        manLinkSelect.add(scrollPane_1);

        JScrollPane manScrollPane = new JScrollPane(manLogField);

        JPopupMenu popupMenu_1 = new JPopupMenu();
        addPopup(manLogField, popupMenu_1);

        JMenuItem mntmSaveLogTo = new JMenuItem("Save log to file");
        mntmSaveLogTo.addActionListener(e -> {
            if (!manLogField.getText().isEmpty()) {
                String fileName = "manual log.txt";
                try (PrintStream out = new PrintStream(manSaveLocation.getText() + File.separator + fileName,
                        "UTF-8")) {
                    out.print(manLogField.getText());
                } catch (IOException ec) {
                    System.out.println(ec.getMessage());
                }
            } else {
                showPopup("Log is empty", "warning");
            }
        });
        mntmSaveLogTo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        popupMenu_1.add(mntmSaveLogTo);

        JSeparator separator_2 = new JSeparator();
        popupMenu_1.add(separator_2);

        JMenuItem mntmClearLog_1 = new JMenuItem("Clear log");
        mntmClearLog_1.addActionListener(e -> {
            if (!manLogField.getText().isEmpty()) {
                manLogField.setText(null);
            }
        });
        mntmClearLog_1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        popupMenu_1.add(mntmClearLog_1);
        manScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        manScrollPane.setBounds(0, 0, 532, 219);
        manLogArea.add(manScrollPane);
        JLabel lblLinkSelect = new JLabel("Select links to be removed:");
        lblLinkSelect.setBounds(10, 60, 227, 25);
        chapterLinkPane.add(lblLinkSelect);

        JButton retrieveLinks = new JButton("Retrieve Links");
        retrieveLinks.setFocusPainted(false);
        retrieveLinks.setFont(new Font("Tahoma", Font.PLAIN, 11));
        retrieveLinks.addActionListener(arg0 -> {
            if (manChapterListURL.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frmNovelGrabber, "URL field is empty.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                manChapterListURL.requestFocusInWindow();
            }
            if (!manChapterListURL.getText().isEmpty()) {
                try {
                    fetchChapters.chapterURLs.clear();
                    listModelChapterLinks.clear();
                    fetchChapters.retrieveLinks();
                } catch (NullPointerException | IllegalArgumentException | IOException err) {
                    JOptionPane.showMessageDialog(frmNovelGrabber, err, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    removeLinks.setEnabled(true);
                }
            }

        });
        retrieveLinks.setBounds(448, 13, 99, 27);
        chapterLinkPane.add(retrieveLinks);

        JPanel textSelectPane = new JPanel();
        textSelectPane.setBounds(10, 314, 557, 78);
        textSelectPane.setBorder(BorderFactory.createTitledBorder("Chapter text select"));
        manualPane.add(textSelectPane);
        textSelectPane.setLayout(null);

        JLabel lblChapterContainerSelector = new JLabel("Chapter container selector:");
        lblChapterContainerSelector.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblChapterContainerSelector.setBounds(20, 11, 153, 25);
        lblChapterContainerSelector.setToolTipText(toolTipStyle
                + "Input chapter wrapping <div> selector following jsoup conventions. For example: .fr-view/.chapter-text etc for <div> class names. #mw-content-text/#chapter-wrapper for <div> id names. More info on jsoup.org</p></html>");

        textSelectPane.add(lblChapterContainerSelector);

        JLabel lblSentenceSelector = new JLabel("Sentence selector:");
        lblSentenceSelector.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblSentenceSelector.setBounds(20, 42, 120, 25);
        lblSentenceSelector
                .setToolTipText(toolTipStyle + "Input html sentence wrapping. Use \"p\" for the paragraph tag.</p></html>");
        textSelectPane.add(lblSentenceSelector);

        manChapterContainer = new JTextField();
        manChapterContainer.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manChapterContainer.setBounds(177, 13, 144, 20);
        textSelectPane.add(manChapterContainer);
        manChapterContainer.setColumns(10);

        manSentenceSelector = new JTextField();
        manSentenceSelector.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manSentenceSelector.setBounds(177, 44, 86, 20);
        textSelectPane.add(manSentenceSelector);
        manSentenceSelector.setColumns(10);

        manUseSentenceSelector = new JCheckBox("Don't use a sentence selector");
        manUseSentenceSelector.setFocusPainted(false);
        manUseSentenceSelector
                .setToolTipText("<html><p width=\"300\">Grab all text from the chapter container.</p></html>");
        manUseSentenceSelector.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manUseSentenceSelector.setBounds(337, 43, 172, 23);
        textSelectPane.add(manUseSentenceSelector);
        manUseSentenceSelector.addItemListener(arg0 -> {
            if (manUseSentenceSelector.isSelected()) {
                manSentenceSelector.setEnabled(false);
            }
            if (!manUseSentenceSelector.isSelected()) {
                manSentenceSelector.setEnabled(true);
            }
        });

        manProgressBar = new JProgressBar();
        manProgressBar.setBounds(15, 560, 430, 27);
        manualPane.add(manProgressBar);

        JButton btnManGrabChapters = new JButton("Grab Chapters");
        btnManGrabChapters.setFocusPainted(false);
        btnManGrabChapters.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnManGrabChapters.setBounds(455, 560, 112, 27);
        manualPane.add(btnManGrabChapters);

        JLabel lblSaveLocation = new JLabel("Save directory:");
        lblSaveLocation.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblSaveLocation.setBounds(20, 512, 118, 25);
        manualPane.add(lblSaveLocation);

        manSaveLocation = new JTextField();
        manSaveLocation.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manSaveLocation.setBounds(126, 514, 319, 25);
        manualPane.add(manSaveLocation);
        manSaveLocation.setColumns(10);

        JButton btnManBrowse = new JButton("Browse...");
        btnManBrowse.setFocusPainted(false);
        btnManBrowse.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnManBrowse.addActionListener(arg0 -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setDialogTitle("Choose destination directory");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);

            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                manSaveLocation.setText(chooser.getSelectedFile().toString());
            }
        });
        btnManBrowse.setBounds(455, 513, 112, 27);
        manualPane.add(btnManBrowse);

        JSeparator separator = new JSeparator();
        separator.setBounds(15, 549, 550, 5);
        manualPane.add(separator);

        JPanel manOptionPane = new JPanel();
        manOptionPane.setBounds(10, 394, 557, 95);
        manualPane.add(manOptionPane);
        manOptionPane.setLayout(null);
        manOptionPane.setBorder(BorderFactory.createTitledBorder("Option select"));

        manCreateToc = new JCheckBox("Create ToC");
        manCreateToc.setToolTipText(
                "<html><p width=\"300\">Will create a \"Table of Contents\" file which can be used to convert all chapter files into a single epub file in calibre.</p></html>");
        manCreateToc.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manCreateToc.setFocusPainted(false);
        manCreateToc.setBounds(6, 20, 81, 23);
        manOptionPane.add(manCreateToc);

        manFileType = new JComboBox<>(fileTypeList);
        manFileType.setFocusable(false);
        manFileType.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manFileType.setBounds(481, 22, 66, 20);
        manOptionPane.add(manFileType);

        JLabel label_1 = new JLabel("File output:");
        label_1.setFont(new Font("Tahoma", Font.PLAIN, 11));
        label_1.setBounds(414, 20, 66, 25);
        manOptionPane.add(label_1);

        manUseNumeration = new JCheckBox("Chapter numeration");
        manUseNumeration.setToolTipText(
                "<html><p width=\"300\">Will add a chapter number infront of the chapter name. Helpful for ordering chapters which don't have a chapter number in their title.</p></html>");
        manUseNumeration.setFocusPainted(false);
        manUseNumeration.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manUseNumeration.setBounds(6, 40, 121, 23);
        manOptionPane.add(manUseNumeration);

        manCheckInvertOrder = new JCheckBox("Invert chapter order");
        manCheckInvertOrder.setFocusPainted(false);
        manCheckInvertOrder.setToolTipText(
                "<html><p width=\"300\">Invert the chapter order and download the last chapter first. Useful if sites list the highest chapter at the top.</p></html>");
        manCheckInvertOrder.setFont(new Font("Tahoma", Font.PLAIN, 11));
        manCheckInvertOrder.setBounds(6, 60, 139, 23);
        manOptionPane.add(manCheckInvertOrder);

        JLabel manWaitTimeLbl = new JLabel("Wait time:");
        manWaitTimeLbl.setToolTipText(
                "<html><p width=\"300\">Time in miliseconds to wait before each chapter grab. We don't want to DDoS afterall :-) (1000 for 1 second).</p></html>");
        manWaitTimeLbl.setBounds(414, 56, 66, 14);
        manOptionPane.add(manWaitTimeLbl);

        manWaitTime = new JTextField();
        manWaitTime.setText("0");
        manWaitTime.setHorizontalAlignment(SwingConstants.CENTER);
        manWaitTime.setColumns(10);
        manWaitTime.setBounds(481, 53, 66, 20);
        manOptionPane.add(manWaitTime);

        // manual chapter download
        btnManGrabChapters.addActionListener(e -> {
            btnManGrabChapters.setEnabled(false);
            // input validation
            if (manChapterListURL.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frmNovelGrabber, "URL field is empty.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                manChapterListURL.requestFocusInWindow();
            } else if (manSaveLocation.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frmNovelGrabber, "Save directory field is empty.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                manSaveLocation.requestFocusInWindow();
            } else if (manChapterContainer.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frmNovelGrabber, "Chapter container selector is empty.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                manChapterContainer.requestFocusInWindow();
            } else if (manWaitTime.getText().isEmpty()) {
                showPopup("Wait time cannot be empty.", "warning");
            } else if (!manWaitTime.getText().matches("\\d+") && !manWaitTime.getText().isEmpty()) {
                showPopup("Wait time must contain numbers.", "warning");
            } else if ((Objects.requireNonNull(manFileType.getSelectedItem()).toString().equals(".txt")) && (manCreateToc.isSelected())) {
                JOptionPane.showMessageDialog(frmNovelGrabber,
                        "Cannot create Table of Contents page from txt files.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                manSaveLocation.requestFocusInWindow();
            } else if ((!manSaveLocation.getText().isEmpty())
                    && (!manChapterListURL.getText().isEmpty())
                    && (!manChapterContainer.getText().isEmpty())
                    && (!manWaitTime.getText().isEmpty())) {
                try {
                    manProgressBar.setStringPainted(true);
                    fetchChapters.manSaveChapters();
                    if (manCreateToc.isSelected()) {
                        fetchChapters.createToc(manSaveLocation.getText(), "manual");
                    }
                    // clear arrays for next call
                    fetchChapters.chapterFileNames.clear();
                    fetchChapters.failedChapters.clear();
                    // Exception handling
                } catch (NullPointerException | IllegalArgumentException | IOException err) {
                    JOptionPane.showMessageDialog(frmNovelGrabber, err, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    manProgressBar.setStringPainted(false);
                    manProgressBar.setValue(0);
                }
            }
            btnManGrabChapters.setEnabled(true);
        });

        // Single Chapter
        getChapterBtn.addActionListener(e -> {
            if (singleChapterLink.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frmNovelGrabber, "URL field is empty.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                singleChapterLink.requestFocusInWindow();
            } else if (!singleChapterLink.getText().isEmpty()) {
                try {
                    progressBar.setStringPainted(true);
                    fetchChapters.saveSingleChapter();
                } catch (NullPointerException | IllegalArgumentException | IOException err) {
                    JOptionPane.showMessageDialog(frmNovelGrabber, err, "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setStringPainted(false);
                    progressBar.setValue(0);
                }
            }
        });

        // All Chapters
        grabChapters.addActionListener(arg0 -> {
            grabChapters.setEnabled(false);
            // input validation
            if (chapterListURL.getText().isEmpty()) {
                showPopup("URL field is empty.", "warning");
                chapterListURL.requestFocusInWindow();
            } else if ((Objects.requireNonNull(fileType.getSelectedItem()).toString().equals(".txt"))
                    && (createTocCheckBox.isSelected())) {
                showPopup("Cannot create a Table of Contents file with .txt file type.", "warning");
            } else if (saveLocation.getText().isEmpty()) {
                showPopup("Save directory field is empty.", "warning");
                saveLocation.requestFocusInWindow();
            } else if ((!chapterAllCheckBox.isSelected())
                    && ((firstChapter.getText().isEmpty()) || (lastChapter.getText().isEmpty()))) {
                showPopup("No chapter range defined.", "warning");
            } else if ((!chapterAllCheckBox.isSelected())
                    && (!firstChapter.getText().matches("\\d+") || !lastChapter.getText().matches("\\d+"))) {
                showPopup("Chapter range must contain numbers.", "warning");
            } else if ((!chapterAllCheckBox.isSelected()) && ((Integer.parseInt(firstChapter.getText()) < 1)
                    || (Integer.parseInt(lastChapter.getText()) < 1))) {
                showPopup("Chapter numbers can't be lower than 1.", "warning");
            } else if ((!chapterAllCheckBox.isSelected())
                    && (Integer.parseInt(lastChapter.getText()) < Integer.parseInt(firstChapter.getText()))) {
                showPopup("Last chapter can't be lower than first chapter.", "warning");
            } else if (waitTime.getText().isEmpty()) {
                showPopup("Wait time cannot be empty.", "warning");
            } else if (!waitTime.getText().matches("\\d+") && !waitTime.getText().isEmpty()) {
                showPopup("Wait time must contain numbers.", "warning");
            } else if ((!saveLocation.getText().isEmpty())
                    && (!chapterListURL.getText().isEmpty())
            ) {
                // grabbing chapter calls
                try {
                    progressBar.setStringPainted(true);
                    fetchChapters.getChapterLinks();
                    if (createTocCheckBox.isSelected()) {
                        fetchChapters.createToc(saveLocation.getText(), "auto");
                    }
                    fetchChapters.chapterFileNames.clear();
                    fetchChapters.failedChapters.clear();
                } catch (NullPointerException | IllegalArgumentException | IOException err) {
                    //showPopup(err.toString(), "error");
                    err.printStackTrace();
                } finally {
                    progressBar.setStringPainted(false);
                    progressBar.setValue(0);
                }
            }
            grabChapters.setEnabled(true);
        });

    }

    private void showPopup(String errorMsg, String kind) {
        switch (kind) {
            case "warning":
                JOptionPane.showMessageDialog(frmNovelGrabber, errorMsg, "Warning", JOptionPane.WARNING_MESSAGE);
                break;
            case "error":
                JOptionPane.showMessageDialog(frmNovelGrabber, errorMsg, "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
}
