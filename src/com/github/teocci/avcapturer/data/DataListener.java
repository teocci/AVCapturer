package com.github.teocci.avcapturer.data;

import java.awt.image.BufferedImage;

public interface DataListener {
	void onDirty(BufferedImage bufferedImage);
}
