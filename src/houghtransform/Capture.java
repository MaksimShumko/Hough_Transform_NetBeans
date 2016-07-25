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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opencv.core.Core;
import org.opencv.core.Mat;
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
    int choiceT;
    Transform transf;
    
    public Capture(CaptureJFrame sync, VideoCapture webCam, int i, int choiceT) {
        this.webCam = webCam;
        syncJFrame = sync;
        this.i = i;
        this.choiceT = choiceT;
        
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
                
        while(!shouldStop) {
            try {
                webCam.read(scr);
                
                // Canny Edge Detector
                Imgproc.Canny(scr, edges, 50, 200, 3, false);
                
                // Hough Transform
                
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
                
                syncJFrame.showVideo(convertMatToBufferedImage(edges), convertMatToBufferedImage(scr));
            } catch (IOException ex) {
                Logger.getLogger(Capture.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}