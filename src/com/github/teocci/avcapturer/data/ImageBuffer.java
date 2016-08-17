package com.github.teocci.avcapturer.data;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;

public class ImageBuffer{

    private int totalLength = 0;
    private final int frameLength;
    private ByteArrayOutputStream byteArrayOutputStream;
    
    public ImageBuffer(int frameLgth, int w, int h) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        frameLength = frameLgth;
        //System.out.println("ImageBuffer: Width x Height --> " + w + "x" + h);
    }
    
    public int fillBuffer(byte[] data, int off, int len, LinkedList<byte[]> YUVQueue) {
        totalLength += len;
        byteArrayOutputStream.write(data, off, len);
        
        if (totalLength == frameLength) {
            
            synchronized (YUVQueue) {
            	YUVQueue.add(byteArrayOutputStream.toByteArray());
            	byteArrayOutputStream.reset();
            }
            
            totalLength = 0;
            System.out.println("received file");
        }
        
        return 0;
    }
}
