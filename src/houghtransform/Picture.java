/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform;

import houghtransform.transform_process.MyTransform;
import houghtransform.transform_process.OpenCV;
import houghtransform.transform_process.Transform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.*;

/**
 *
 * @author Maksim
 */
public class Picture {
    private static BufferedImage convertMatToBufferedImage(Mat mat) {  
        byte[] data = new byte[mat.width() * mat.height() * (int)mat.elemSize()];  
        int type;  
        mat.get(0, 0, data);  
  
        switch (mat.channels()) {    
            case 1:    
                type = BufferedImage.TYPE_BYTE_GRAY;    
                break;    
            case 3:    
                type = BufferedImage.TYPE_3BYTE_BGR;    
                // bgr to rgb    
                byte b;    
                for(int i=0; i<data.length; i=i+3) {    
                    b = data[i];    
                    data[i] = data[i+2];    
                    data[i+2] = b;    
                }    
                break;    
            default:    
                throw new IllegalStateException("Unsupported number of channels");  
        }    
          
        BufferedImage out = new BufferedImage(mat.width(), mat.height(), type);  
  
        out.getRaster().setDataElements(0, 0, mat.width(), mat.height(), data);  
          
        return out;  
    }  
    
    public Picture(String fileName, int choiceT) throws IOException {
        
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Mat scr = Imgcodecs.imread(fileName);
        if(scr.empty()) throw new IOException("File not exist!!!");
        
        Mat edges = new Mat();
        // Canny Edge Detector
        Imgproc.Canny(scr, edges, 50, 200, 3, false);
        
        // Hough Transform
        Transform transf;
        switch(choiceT) {
            case 1:
                transf = new OpenCV();
                break;
            case 2:
                transf = new MyTransform();
                break;
            default:
                throw new IOException("Transform wasn't choiced!!!");
        }

        transf.houghTransform(edges);
        scr = transf.drawLines(scr);
        
        PictureJFrame x = new PictureJFrame(convertMatToBufferedImage(edges), convertMatToBufferedImage(scr));
        x.setVisible(true);
        
    }    
}
