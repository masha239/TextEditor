

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.ScrollPane;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.io.*;
import java.awt.event.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;


public class TextEditor extends JFrame {
    private volatile List<Integer> indexes;
    private volatile List<Integer> lengths;
    private volatile int currentIndex;
    private volatile boolean regex;
    private volatile String text;
    private volatile String expression;
    private volatile JTextArea textArea;

    String getTextFromFile(String fileName) {

        try (FileInputStream inFile = new FileInputStream(fileName);) {
            byte[] str = new byte[inFile.available()];
            inFile.read(str);
            String text = new String(str);
            return text;
        } catch (Exception e) {
            return null;
        }
    }

    void saveTextToFile(String text, String fileName) {
        try (FileWriter writer = new FileWriter(fileName, false)) {
            writer.write(text);
        } catch (IOException ex) {
        }
    }

    void showExpression() {
        textArea.setCaretPosition(indexes.get(currentIndex) + lengths.get(currentIndex));
        textArea.select(indexes.get(currentIndex), indexes.get(currentIndex) + lengths.get(currentIndex));
        textArea.grabFocus();
    }

    public class Searcher extends SwingWorker<Integer, Integer> {

        @Override
        protected Integer doInBackground() throws Exception {
            List<Integer> indexes1 = new ArrayList<>();
            List<Integer> lengths1 = new ArrayList<>();
            if (regex) {
                Pattern pattern;
                pattern = Pattern.compile(expression);
                Matcher matcher = pattern.matcher(text);
                while (matcher.find()) {
                    indexes1.add(matcher.start());
                    lengths1.add(matcher.end() - matcher.start());
                }
            } else {
                boolean ok;
                for (int i = 0; i < text.length() - expression.length() + 1; i++) {
                    ok = true;
                    for (int j = 0; j < expression.length(); j++) {
                        if (text.charAt(i + j) != expression.charAt(j)) {
                            ok = false;
                            break;
                        }
                    }
                    if (ok) {
                        indexes1.add(i);
                        lengths1.add(expression.length());
                    }
                }
            }
            indexes.clear();
            lengths.clear();
            indexes.addAll(indexes1);
            lengths.addAll(lengths1);
            currentIndex = 0;
            if (indexes.size() != 0) {
                showExpression();
            }
            return indexes.size();
        }
    }

    public TextEditor() {
        super("Text Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 400);
        setLocationRelativeTo(null);

        indexes = new ArrayList<>();
        lengths = new ArrayList<>();
        currentIndex = 0;
        regex = false;
        text = "";
        expression = "";

        JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getDefaultDirectory());
        fileChooser.setName("FileChooser");
        add(fileChooser);

        JTextField input = new JTextField(16);
        input.setName("SearchField");

        textArea = new JTextArea();
        textArea.setName("TextArea");
        JScrollPane pane = new JScrollPane(textArea);
        pane.setName("ScrollPane");

        ImageIcon saveIcon = new ImageIcon("001-save.png");
        JButton button1 = new JButton(saveIcon);
        button1.setName("SaveButton");
        button1.addActionListener(actionEvent -> {
            String text = textArea.getText();
            fileChooser.showSaveDialog(null);
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            saveTextToFile(text, fileName);
        });

        ImageIcon loadIcon = new ImageIcon("002-folder.png");
        JButton button2 = new JButton(loadIcon);
        button2.setName("OpenButton");
        button2.addActionListener(actionEvent -> {
            fileChooser.showOpenDialog(null);
            String fileName = fileChooser.getSelectedFile().getAbsolutePath();
            String text = getTextFromFile(fileName);
            textArea.setText(text);
        });

        ImageIcon searchIcon = new ImageIcon("003-search.png");
        JButton searchButton = new JButton(searchIcon);
        searchButton.setName("StartSearchButton");
        searchButton.addActionListener(actionEvent -> {
            text = textArea.getText();
            expression = input.getText();
            (new Searcher()).execute();
        });

        ImageIcon previousMatchIcon = new ImageIcon("004-back.png");
        JButton prevButton = new JButton(previousMatchIcon);
        prevButton.setName("PreviousMatchButton");
        prevButton.addActionListener(actionEvent -> {
            if (currentIndex > 0) {
                currentIndex--;
            } else {
                currentIndex = indexes.size() - 1;
            }
            showExpression();
        });

        ImageIcon nextMatchIcon = new ImageIcon("005-next.png");
        JButton nextButton = new JButton(nextMatchIcon);
        nextButton.setName("NextMatchButton");
        nextButton.addActionListener(actionEvent -> {
            if (currentIndex < indexes.size() - 1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
            showExpression();
        });

        JCheckBox useRegExCheckbox = new JCheckBox("Use regex");
        useRegExCheckbox.setName("UseRegExCheckbox");
        useRegExCheckbox.addActionListener(actionEvent -> {
            regex = !regex;
        });

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setName("MenuFile");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.setName("MenuOpen");
        openMenuItem.addActionListener(actionEvent -> {
            button2.doClick();
        });

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setName("MenuSave");
        saveMenuItem.addActionListener(actionEvent -> {
            button1.doClick();
        });

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setName("MenuExit");
        exitMenuItem.addActionListener(actionEvent -> {
            System.exit(0);
        });

        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        JMenu searchMenu = new JMenu("Search");
        searchMenu.setName("MenuSearch");
        searchMenu.setMnemonic(KeyEvent.VK_S);

        JMenuItem startSearchItem = new JMenuItem("Start search");
        startSearchItem.setName("MenuStartSearch");
        startSearchItem.addActionListener(actionEvent -> {
            searchButton.doClick();
        });

        JMenuItem prevMatchItem = new JMenuItem("Previous match");
        prevMatchItem.setName("MenuPreviousMatch");
        prevMatchItem.addActionListener(actionEvent -> {
            prevButton.doClick();
        });

        JMenuItem nextMatchItem = new JMenuItem("Next match");
        nextMatchItem.setName("MenuNextMatch");
        nextMatchItem.addActionListener(actionEvent -> {
            nextButton.doClick();
        });

        JMenuItem useRegexItem = new JMenuItem("Use regular expressions");
        useRegexItem.setName("MenuUseRegExp");
        useRegexItem.addActionListener(actionEvent -> {
            useRegExCheckbox.doClick();
        });

        searchMenu.add(startSearchItem);
        searchMenu.add(prevMatchItem);
        searchMenu.add(nextMatchItem);
        searchMenu.add(useRegexItem);

        menuBar.add(fileMenu);
        menuBar.add(searchMenu);
        setJMenuBar(menuBar);

        JPanel myPanel = new JPanel();
        myPanel.setLayout(new FlowLayout());
        myPanel.add(button1);
        myPanel.add(button2);
        myPanel.add(input);
        myPanel.add(searchButton);
        myPanel.add(prevButton);
        myPanel.add(nextButton);
        myPanel.add(useRegExCheckbox);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(myPanel, BorderLayout.NORTH);
        getContentPane().add(pane, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TextEditor();
            }
        });

    }
}
