package com.github.teocci.avcapturer.ui;

import com.github.teocci.avcapturer.data.DataListener;
import com.github.teocci.avcapturer.io.AudioServer;
import com.github.teocci.avcapturer.io.VideoServer;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class ServerUI extends JLabel implements DataListener {
    private LinkedList<BufferedImage> queue = new LinkedList<BufferedImage>();
    private static final int MAX_BUFFER = 15;

    BufferedImage image, lastFrame;

    public ServerUI(int videoPort, int audioPort) {
        VideoServer videoServer = new VideoServer(videoPort);
        videoServer.setOnDataListener(this);
        videoServer.start();

        AudioServer audioServer = new AudioServer(audioPort);
        audioServer.start();
    }

    @Override
    public void paint(Graphics g) {
        synchronized (queue) {
            if (queue.size() > 0) {
                lastFrame = queue.poll();
            }
        }
        if (lastFrame != null) {
            this.setSize(getPreferredSize());
            rotateImage(lastFrame, g);
        } else if (image != null) {
            rotateImage(image, g);
            //g.drawImage(image, 0, 0, null);
        }
    }

    private void rotateImage(BufferedImage img, Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        double rotationRequired = Math.toRadians (-90);
        double locationX = img.getWidth() / 2;
        double locationY = img.getHeight() / 2;

        //System.out.println("Rotate: Width x Height --> " + img.getWidth() + "x" + img.getHeight() );
        AffineTransform tx = AffineTransform.getRotateInstance(rotationRequired, locationX, locationY);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);

        // Drawing the rotated image at the required drawing locations
        g2d.drawImage(img, tx, this);
    }

    private void updateUI(BufferedImage bufferedImage) {
        synchronized (queue) {
            if (queue.size() == MAX_BUFFER) {
                lastFrame = queue.poll();
            }
            queue.add(bufferedImage);
        }

        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (image == null) {
            System.out.println("getPreferredSize (image == null): Width x Height --> " + 480 + "x" + 640);
            return new Dimension(480, 640); // init window size
        } else {
            System.out.println("getPreferredSize: Width x Height --> " + image.getWidth(null) + "x" + image.getHeight(null) );
            return new Dimension(image.getHeight(null), image.getWidth(null));
        }
    }

    @Override
    public void onDirty(BufferedImage bufferedImage) {
        // TODO Auto-generated method stub
        updateUI(bufferedImage);
    }
}
