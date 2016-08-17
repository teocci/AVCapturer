package com.github.teocci.avcapturer.data;

import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class BufferManager extends Thread {
    private ImageBuffer[] bufferQueue;
    private int fillCount = 0;
    private final int frameLength;
    private int remained = 0;
    private static final int MAX_BUFFER_COUNT = 2;
    private int width, height;
    private LinkedList<byte[]> YUVQueue = new LinkedList<>();
    private DataListener listener;
    
    public BufferManager(int frameLgth, int w, int h) {
        // TODO Auto-generated constructor stub
    	width = w;
    	height = h;
        frameLength = frameLgth;
        bufferQueue = new ImageBuffer[MAX_BUFFER_COUNT];
        for (int i = 0; i < MAX_BUFFER_COUNT; ++i) {
            bufferQueue[i] = new ImageBuffer(frameLength, w, h);
        }
    }
    
	public void fillBuffer(byte[] data, int len) {
		fillCount = fillCount % MAX_BUFFER_COUNT;
		if (remained != 0) {
			if (remained < len) {
				bufferQueue[fillCount].fillBuffer(data, 0, remained, YUVQueue);
				++fillCount;
				if (fillCount == MAX_BUFFER_COUNT)
					fillCount = 0;
				bufferQueue[fillCount].fillBuffer(data, remained, len - remained, YUVQueue);
				remained = frameLength - len + remained;
			} else if (remained == len) {
				bufferQueue[fillCount].fillBuffer(data, 0, remained, YUVQueue);
				remained = 0;
				++fillCount;
				if (fillCount == MAX_BUFFER_COUNT)
                    fillCount = 0;
			} else {
				bufferQueue[fillCount].fillBuffer(data, 0, len, YUVQueue);
				remained = remained - len;
			}
		} else {
			bufferQueue[fillCount].fillBuffer(data, 0, len, YUVQueue);

			if (len < frameLength) {
				remained = frameLength - len;
			} else {
				++fillCount;
				if (fillCount == MAX_BUFFER_COUNT)
				    fillCount = 0;
			}
		}
	}
    
    public void setOnDataListener(DataListener dataListener) {
    	listener = dataListener;
    	start();
    }
    
    public void close() {
    	interrupt();
    	try {
			join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    @Override
    public void run() {
    	// TODO Auto-generated method stub
    	super.run();
    	
    	while (!Thread.currentThread().isInterrupted()) {
    		byte[] data;
    		synchronized (YUVQueue) {
    			data = YUVQueue.poll();
    			
    			if (data != null) {
    				long t = System.currentTimeMillis();
    				BufferedImage bufferedImage;
    				int[] rgbArray = Utils.convertYUVtoRGB(data, width, height);

					//System.out.println("Width x Height --> " + width + "x" + height);
    				bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_565_RGB);
    				bufferedImage.setRGB(0, 0, width, height, rgbArray, 0, width);
    				
                    listener.onDirty(bufferedImage);
                    System.out.println("time cost = " + (System.currentTimeMillis() - t));
    			}
    		}
    	}
    }
}
