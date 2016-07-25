/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houghtransform.transform_process;

import org.opencv.core.Mat;

/**
 *
 * @author Maksim
 */
public interface Transform {
    public void houghTransform(Mat edges);
    public Mat drawLines(Mat scr);
}
