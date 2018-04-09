/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencvbasics;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 * FXML Controller class
 *
 * @author asmin
 */
public class ViewController implements Initializable {

    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;
    @FXML
    private ImageView histogram;
    @FXML
    private CheckBox grayscale;

    private VideoCapture videoCapture = new VideoCapture();
    private ScheduledExecutorService timer;
    private boolean isCamerActive = false;
    @FXML
    private Insets x1;
    @FXML
    private CheckBox logoCheckBox;
    private Mat logo;
    @FXML
    private AnchorPane AnchorPane;

    private boolean gray = false;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    private void startCamera(ActionEvent event) {
        // TODO
        if (!isCamerActive) {
            this.videoCapture.open(0);
            if (this.videoCapture.isOpened()) {
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = new Mat();
                        videoCapture.read(frame);
                        if (logoCheckBox.isSelected() && logo != null) {
                            Rect roi = new Rect(frame.cols() - logo.cols(), frame.rows() - logo.rows(), logo.cols(), logo.rows());
                            Mat imageROI = frame.submat(roi);
                            // add the logo: method #1

                            Core.addWeighted(imageROI, 1.0, logo, 0.7, 0.0, imageROI);
                            // add the logo: method #2
                            // Mat mask = logo.clone();
                            // logo.copyTo(imageROI, mask);
                        }
                        if (grayscale.isSelected()) {
                            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                            gray = true;
                        } else {
                            gray = false;
                        }
                        
//                        MatOfByte buffer = new MatOfByte();
//                        Imgcodecs.imencode(".png", frame, buffer);
//                        Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
                        
                        Image imageToShow=mat2Image(frame);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                currentFrame.setImage(imageToShow);
                                showHistogram(frame);
                            }
                        });
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                this.button.setText("Stop Camera");
                isCamerActive = true;
            } else {
                System.out.println("Could not open camera");
            }
        } else {
            this.button.setText("Start Camera");
            isCamerActive = false;
            stopCamera();
        }
//        
    }

    @FXML
    protected void loadLogo() {
        if (logoCheckBox.isSelected()) {
            this.logo = Imgcodecs.imread("res/1.jpg");
            System.out.println(logo);
        }
    }

    private void showHistogram(Mat frame) {
        // split the frames in multiple images
        List<Mat> images = new ArrayList<Mat>();
        Core.split(frame, images);

        // set the number of bins at 256
        MatOfInt histSize = new MatOfInt(256);
        // only one channel
        MatOfInt channels = new MatOfInt(0);
        // set the ranges
        MatOfFloat histRange = new MatOfFloat(0, 256);

        // compute the histograms for the B, G and R components
        Mat hist_b = new Mat();
        Mat hist_g = new Mat();
        Mat hist_r = new Mat();

        // B component or gray image
        Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);

        // G and R components (if the image is not in gray scale)
        if (!gray) {
            Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
            Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);
        }

        int hist_w = 150;
        int hist_h = 150;
        int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);
        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));
        Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        if (!gray) {
            Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        }

        for (int i = 1; i < histSize.get(0, 0)[0]; i++) {
            Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])), new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);
            if (!gray) {
                Imgproc.line(histImage,
                        new Point(bin_w * (i - 1),
                                hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
                        new Point(bin_w * (i),
                                hist_h - Math.round(hist_g.get(i, 0)[0])),
                        new Scalar(0, 255, 0), 2, 8, 0);
                Imgproc.line(histImage,
                        new Point(bin_w * (i - 1),
                                hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
                        new Point(bin_w * (i),
                                hist_h - Math.round(hist_r.get(i, 0)[0])),
                        new Scalar(0, 0, 255), 2, 8, 0);
            }
        }
        Image histo = mat2Image(histImage);
        histogram.setImage(histo);

    }

    private Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", frame, buffer);
        Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
        return imageToShow;
    }

    public void stopCamera() {
        System.out.println("inside stop camera");
        this.videoCapture.release();

    }
}
