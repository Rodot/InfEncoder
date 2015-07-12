/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package infencoder;

import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.out;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Rodot
 */
public class InfEncoder {

    void InfEncoder() {
    }

    List<Byte> generateOutput(String gameName, BufferedImage gameIcon, BufferedImage[] slides) {
        List<Byte> output = new ArrayList();
        //token
        String token = "gbinf";
        for (char thisChar : token.toCharArray()) {
            output.add((byte) thisChar);
        }
        output.add((byte) 0x00); //ending null character
        //version byte
        output.add((byte) 0x01);
        //name
        for (int i = 0; i < 18; i++) {
            if (i < gameName.length()) {
                output.add((byte) gameName.charAt(i));
            } else {
                output.add((byte) 0x00);
            }
        }
        output.add((byte) 0x00); //ending null character
        //game icon
        output.addAll(encodeBitmap(gameIcon));
        //number of slides
        output.add((byte) slides.length);
        //encode all the slides
        for (BufferedImage thisBitmap : slides) {
            output.addAll(encodeBitmap(thisBitmap));
        }
        return output;
    }

    List<Byte> encodeBitmap(BufferedImage bitmap) {
        if (bitmap == null) {
            return null;
        } else {
            if ((bitmap.getWidth() > 200) || (bitmap.getHeight() > 200)) {
                return null;
            }
        }
        List<Byte> returnValue = new ArrayList<>();
        //logo width and height
        //int width = ((Math.min(bitmap.getWidth(), 84) + 7) / 8) * 8; //round to the closest larger multiple of 8
        //int height = Math.min(bitmap.getHeight(), 48);
        //returnValue.add((byte) width);
        //returnValue.add((byte) height);
        //logo
        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x += 8) {
                byte thisByte = 0;
                for (int b = 0; b < 8; b++) {
                    int value = 0xFFFF;
                    if (x + b < bitmap.getWidth()) {
                        int rgb = bitmap.getRGB(x + b, y);
                        int red = (rgb >> 16) & 0x000000FF;
                        int green = (rgb >> 8) & 0x000000FF;
                        int blue = (rgb) & 0x000000FF;
                        value = (red + green + blue)/3;
                    }
                    thisByte <<= 1;
                    if (value < 255/2) {
                        thisByte++;
                    }
                }
                returnValue.add(thisByte);
            }
        }
        return returnValue;
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
