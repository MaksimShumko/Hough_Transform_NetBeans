/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform.transform_process;

import java.io.IOException;
import java.util.ArrayList;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Maksim
 */
public class MyTransform implements Transform {
    ArrayList<Point> _lines;
    double theta = 0.0174;
    double threshold = 100;

    int[] _accu;
    int _accu_w;
    int _accu_h;
    int _img_w;
    int _img_h;
    
    public MyTransform() {
        //_lines = new Mat();
    }
    
    @Override
    public void houghTransform(Mat edges) {
        Mat _edges = edges.clone();
        
        double radian = Math.PI/180;
        int degrees = (int) Math.floor(theta * 180/Math.PI + 0.5);

        int w = _edges.cols();
        int h = _edges.rows();

        _edges.convertTo(_edges, CvType.CV_64FC3);
        int size = w * h;
        double[] img_data = new double[size];
        _edges.get(0, 0, img_data); // Gets all pixels
        
        _img_w = w;                         //Number of columns
        _img_h = h;                         //Number of lines

        //Create the accumulator
        double hough_h = ((Math.sqrt(2.0) * (double)(h > w ? h : w)) / 2.0);
        _accu_h = (int) (hough_h * 2.0); // -r -> +r
        _accu_w = 180;

        _accu = new int [_accu_h * _accu_w];
        for (int i = 0; i < _accu_h * _accu_w; i++) {
            _accu[i] = 0;
        }

        double center_x = w / 2;
        double center_y = h / 2;

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (img_data[(y * w) + x] > 250) {
                    for (int t = 0; t < 180; t = t + degrees) {
                        // y = x * cos( theta ) + y * sin( theta )
                        double r = (((double)x - center_x) * Math.cos((double)t * radian)) +
                                   (((double)y - center_y) * Math.sin((double)t * radian));
                        _accu[(int)((Math.floor(r + hough_h) * 180.0)) + t]++;
                    }
                }
            }
        }

        ArrayList<Point> lines = new ArrayList<>();

        if (_accu.length == 0) try {
            throw new IOException("MyTransform: _accu == 0");
        } catch (IOException ex) {
            System.out.println(ex);
        }

        for (int r = 0; r < _accu_h; r++) {
            for (int t = 0; t < _accu_w; t++) {
                // Searching in the accumulator a value greater
                //or equal to the set threshold
                if (((int)_accu[(r * _accu_w) + t]) >= threshold) {
                    // Is this point a local maxima (9x9)
                    int max = _accu[(r * _accu_w) + t];
                    ////////////////////////////////
                    for (int ly = -4; ly <= 4; ly++) {
                        for (int lx = -4; lx <= 4; lx++) {
                            if (((ly + r) >= 0 && (ly + r) < _accu_h) &&
                                ((lx + t) >= 0 && (lx + t) < _accu_w)) {
                                if ((int)_accu[((r + ly) * _accu_w) + (t + lx)] > max) {
                                    max = _accu[((r + ly) * _accu_w) + (t + lx)];
                                    ly = lx = 5;
                                }
                            }
                        }
                    }
                    /////////////////////////////////
                    if (max > (int)_accu[(r * _accu_w) + t]) continue;

                    Point point = new Point();
                    point.x = r;
                    point.y = t * radian;
                    lines.add(point);
                }
            }
        }
        _lines = lines;
    }
    
    @Override
    public Mat drawLines(Mat scr) {
        for (Point pt : _lines) {
            double r = pt.x, t = pt.y;
            Point pt1 = new Point();
            Point pt2 = new Point();
            if (t/(Math.PI/180) >= 45 && t/(Math.PI/180) <= 135) {
                pt1.x = -1000;
                pt2.x = 1000;
                pt1.y = ((r - _accu_h/2) - (-1000 - _img_w / 2) * Math.cos(t)) / Math.sin(t) + _img_h/2;
                pt2.y = ((r - _accu_h/2) - (1000 - _img_w / 2) * Math.cos(t)) / Math.sin(t) + _img_h/2;
            } else {
                pt1.y = -1000;
                pt2.y = 1000;
                pt1.x = ((r - _accu_h/2) - (-1000 - _img_h / 2) * Math.sin(t)) / Math.cos(t) + _img_w/2;
                pt2.x = ((r - _accu_h/2) - (1000 - _img_h / 2) * Math.sin(t)) / Math.cos(t) + _img_w/2;
            }
            //Drawing the lines
            Imgproc.line(scr, pt1, pt2, new Scalar(0, 0, 255), 1);
        }
        return scr;
    }    
}
