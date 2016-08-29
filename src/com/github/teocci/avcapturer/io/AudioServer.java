package com.github.teocci.avcapturer.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by teocci on 8/10/16.
 */
public class AudioServer extends Thread {
    private static ServerSocket server;

    private AudioInputStream audioInputStream;
    private static AudioInputStream ais;

    private static AudioFormat format;

    private static boolean stopStream = false;
    private byte[] tempBuffer;
    private int socketPort = 9990;
    private int packetSize = 120000;

    public AudioServer(int audioPort) {
        socketPort = audioPort;
        tempBuffer = new byte[4000];
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        super.run();

        System.out.println("Server's waiting on port: " + socketPort);


        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        Socket socket = null;
        ByteArrayOutputStream byteArray = null;
        stopStream = false;

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
                byte[] audioData = null;
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
                        element = parser.parse(msg);
                    } catch (JsonParseException e) {
                        System.out.println("exception: " + e);
                        isJSON = false;
                    }
                    if (isJSON && element != null) {
                        JsonObject obj = element.getAsJsonObject();
                        element = obj.get("type");

                        if (element != null && element.getAsString().equals("data")) {
                            int foo = receiveJSON(obj);
                            //packetSize = foo > packetSize? foo : packetSize;
                            packetSize = foo * 16;

                            System.out.println("packetSize: " + packetSize + " foo: " + foo);
                            audioData = new byte[packetSize];
                            break;
                        }
                    } else {
                        byteArray.write(buff, 0, len);
                        break;
                    }
                }

                if (audioData != null) {
                    sendOKResponse(outputStream);
                    AudioInputStream ais = new AudioInputStream(inputStream, format, packetSize);
                    // read mic data
                    while ((len = inputStream.read(audioData)) != -1) {
                        /*InputStream baiss = new ByteArrayInputStream(audioData);
                        audioInputStream = new AudioInputStream(baiss, format, len);*/

                        toSpeaker(audioData);

                        //micBufferManager.fillBuffer(audioData, len);

                        /*byte[] finalReceiveData = receiveData;

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                toSpeaker(finalReceiveData);
                            }
                        }).start();*/
                    }
                }

                /*if (micBufferManager != null) {
                    micBufferManager.close();
                }*/
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                System.out.println("finally");
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

            } catch (IOException e) {
            }
        }

        /*byte[] receiveData = new byte[60480];
        // ( 1280 for 16 000Hz and 3584 for 44 100Hz (use AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) to get the correct size)

        format = new AudioFormat(sampleRate, 16, 1, true, false);

        while (status == true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);

            serverSocket.receive(receivePacket);

            ByteArrayInputStream baiss = new ByteArrayInputStream(
                    receivePacket.getData());

            ais = new AudioInputStream(baiss, format, receivePacket.getLength());

            // A thread solve the problem of chunky audio
            new Thread(new Runnable() {
                @Override
                public void run() {
                    toSpeaker(receivePacket.getData());
                }
            }).start();
        }*/
    }

    private static void sendOKResponse(BufferedOutputStream outputStream) throws IOException {
        JsonObject jsonObj = new JsonObject();
        jsonObj.addProperty("state", "ok");
        outputStream.write(jsonObj.toString().getBytes());
        outputStream.flush();
    }

    public static int receiveJSON(JsonObject obj) {
        JsonElement element = null;
        element = obj.get("length");
        int length = element.getAsInt();
        element = obj.get("channel");
        int channel = element.getAsInt() == 16 ? 1 : 2;
        element = obj.get("encoding");
        int encoding = element.getAsInt() == 2 ? 16 : 8;
        element = obj.get("rate");
        int rate = element.getAsInt();

        System.out.println("Length: " + length +
                " Channel: " + channel +
                " Encoding: " + encoding +
                " Rate: " + rate);

         format = new AudioFormat(rate, encoding, channel, true, false);

        return length;
    }

    public static synchronized void toSpeaker(byte[] soundBytes) {
        try {

            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            sourceDataLine.open(format);

            FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(6.0f);

            sourceDataLine.start();
            System.out.println("Format: " + sourceDataLine.getFormat());

            sourceDataLine.write(soundBytes, 0, soundBytes.length);
            //System.out.println(soundBytes.toString());

            sourceDataLine.drain();
            sourceDataLine.close();
            sourceDataLine = null;
        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
}
