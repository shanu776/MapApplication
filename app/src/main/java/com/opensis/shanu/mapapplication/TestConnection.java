package com.opensis.shanu.mapapplication;

import android.annotation.SuppressLint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by Shanu on 2/24/2017.
 */

public class TestConnection {
    @SuppressLint("NewApi")
    public static boolean pingHost() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("192.168.1.101", 8080), 2000);
            return true;
        } catch (IOException e) {
            return false; // Either timeout or unreachable or failed DNS lookup.
        }
    }

    }

