package com.github.teocci.avcapturer.io;

import com.github.teocci.avcapturer.data.BufferManager;
import com.github.teocci.avcapturer.data.DataListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VideoServer extends Thread {
	private ServerSocket server;
	private DataListener dataListener;
	private BufferManager bufferManager;
	private int socketPort;

	public VideoServer(int port) {
		socketPort = port;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		System.out.println("server's waiting on port: " + socketPort);
		BufferedInputStream inputStream = null;
		BufferedOutputStream outputStream = null;
		Socket socket = null;
		ByteArrayOutputStream byteArray = null;
		try {

			server = new ServerSocket(socketPort);
			while (!Thread.currentThread().isInterrupted()) {
				if (byteArray != null)
					byteArray.reset();
				else
					byteArray = new ByteArrayOutputStream();

				socket = server.accept();
				System.out.println("new socket: " + server.getLocalPort());
				
				inputStream = new BufferedInputStream(socket.getInputStream());
				outputStream = new BufferedOutputStream(socket.getOutputStream());
				
				byte[] buff = new byte[256];
				byte[] imageBuff = null;
				int len;
				String msg;
				// read msg
				while ((len = inputStream.read(buff)) != -1) {
					msg = new String(buff, 0, len);
					// JSON analysis
	                JsonParser parser = new JsonParser();
	                boolean isJSON = true;
	                JsonElement element = null;
	                try {
	                    element =  parser.parse(msg);
	                }
	                catch (JsonParseException e) {
	                    System.out.println("exception: " + e);
	                    isJSON = false;
	                }
	                if (isJSON && element != null) {
	                    JsonObject obj = element.getAsJsonObject();
	                    element = obj.get("type");
	                    if (element != null && element.getAsString().equals("data")) {
	                        element = obj.get("length");
	                        int length = element.getAsInt();
	                        element = obj.get("width");
	                        int width = element.getAsInt();
	                        element = obj.get("height");
	                        int height = element.getAsInt();
	                        
	                        imageBuff = new byte[length];
                            bufferManager = new BufferManager(length, width, height);
                            bufferManager.setOnDataListener(dataListener);
                            break;
	                    }
	                }
	                else {
	                    byteArray.write(buff, 0, len);
	                    break;
	                }
				}
				
				if (imageBuff != null) {
				    JsonObject jsonObj = new JsonObject();
		            jsonObj.addProperty("state", "ok");
		            outputStream.write(jsonObj.toString().getBytes());
		            outputStream.flush();
		            
		            // read image data
				    while ((len = inputStream.read(imageBuff)) != -1) {
	                    bufferManager.fillBuffer(imageBuff, len);
	                }
				}
				
				if (bufferManager != null) {
					bufferManager.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
					outputStream = null;
				}
				
				if (inputStream != null) {
					inputStream.close();
					inputStream = null;
				}

				if (socket != null) {
					socket.close();
	                socket = null;
				}
				
				if (byteArray != null) {
					byteArray.close();
				}
				
			} catch (IOException e) {}
		}
	}

	public void setOnDataListener(DataListener listener) {
		dataListener = listener;
	}
}
