/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform1;

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
    
    public Picture(String fileName) throws IOException {
        
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Mat scr = Imgcodecs.imread(fileName);
        if(scr.empty()) throw new IOException("File not exist!!!");
        
        Mat edges = new Mat();
        // Canny Edge Detector
        Imgproc.Canny(scr, edges, 50, 200, 3, false);
                
        Mat lines = new Mat();
        // Hough Transform
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 100, 0, 0, 0, Math.PI);
        
        for (int i = 1; i < lines.rows(); i++) 
        {
            double[] vec = lines.get(i, 0);
            double rho = vec[0], theta1 = vec[1];
            Point pt1 = new Point();
            Point pt2 = new Point();
            double a = Math.cos(theta1), b = Math.sin(theta1);
            double x0 = a*rho, y0 = b*rho;
            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            
            Imgproc.line(scr, pt1, pt2, new Scalar(0,0,255), 1);
        }
        
        PictureJFrame x = new PictureJFrame(convertMatToBufferedImage(edges), convertMatToBufferedImage(scr));
        x.setVisible(true);
        
    }    
}
