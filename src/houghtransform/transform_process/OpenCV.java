/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform.transform_process;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Maksim
 */
public class OpenCV implements Transform {
    Mat lines;
    
    public OpenCV() {
        lines = new Mat();
    }
            
    @Override
    public void houghTransform(Mat edges) {
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 100, 0, 0, 0, Math.PI);
    }
    
    @Override
    public Mat drawLines(Mat scr) {
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
        return scr;
    }
}
