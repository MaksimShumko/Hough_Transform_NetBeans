/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform1;

import java.awt.image.BufferedImage;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author Maksim
 */
public class Capture implements Runnable{

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
    
    private final VideoCapture webCam;
    public final Thread t;
    private boolean shouldStop = false;
    static CaptureJFrame syncJFrame;
    int i;
    
    public Capture(CaptureJFrame sync, VideoCapture webCam, int i) {
        this.webCam = webCam;
        syncJFrame = sync;
        this.i = i;
        
        t = new Thread(this, "Thread " + i);
    }
    
    public void start() {
        t.start();
    }
    
    public void stop() {
        shouldStop = true;
    }
    
    @Override
    public void run() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        
        Mat scr = new Mat();
        Mat edges = new Mat();
        Mat lines = new Mat();
        
        while(!shouldStop) {
            webCam.read(scr);
        
            // Canny Edge Detector
            Imgproc.Canny(scr, edges, 50, 200, 3, false);
            
            // Hough Transform
            Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 100, 0, 0, 0, Math.PI);
        
            for (int x = 1; x < lines.rows(); x++) {
                double[] vec = lines.get(x, 0);
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
            syncJFrame.showVideo(convertMatToBufferedImage(edges), convertMatToBufferedImage(scr));
        }
    }  
}