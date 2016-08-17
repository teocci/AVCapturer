package com.github.teocci.avcapturer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AVCapturer extends JFrame {

    final static int gapSize = 0;
    GridLayout gl = new GridLayout(2, 3);

    public AVCapturer(String name) {
        super(name);
        setResizable(false);
    }

    public void addComponentsToPane(final Container pane) {
        final JPanel jp = new JPanel();
        jp.setLayout(gl);

        //Set up components preferred size
        jp.add(new ServerUI(8880, 9990));
        /*jp.add(new ServerUI(8293));
        jp.add(new ServerUI(8456));
        jp.add(new ServerUI(8893));
        jp.add(new ServerUI(8894));
        jp.add(new ServerUI(8895));*/
        jp.setPreferredSize(new Dimension(1600, 900));

        //Set up the horizontal gap value
        gl.setHgap(gapSize);
        //Set up the vertical gap value
        gl.setVgap(gapSize);
        //Set up the layout of the buttons
        gl.layoutContainer(jp);

        pane.add(jp, BorderLayout.NORTH);
        pane.add(new JSeparator(), BorderLayout.SOUTH);
    }

    public static void main(String[] args) {
        AVCapturer f = new AVCapturer("Monitor");

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        //Set up the content pane.
        f.addComponentsToPane(f.getContentPane());
        //Display the window.
        f.pack();
        f.setVisible(true);
    }
}