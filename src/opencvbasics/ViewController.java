/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencvbasics;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
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
    private CheckBox grayscale;

    private VideoCapture videoCapture = new VideoCapture();
    private ScheduledExecutorService timer;
    private boolean isCamerActive = false;

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
                        if (grayscale.isSelected()) {
                            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                        }

                        MatOfByte buffer = new MatOfByte();
                        Imgcodecs.imencode(".png", frame, buffer);
                        Image imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                currentFrame.setImage(imageToShow);
                            }
                        });
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
                this.button.setText("Stop Camera");
                isCamerActive=true;
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

    public void stopCamera() {
        System.out.println("inside stop camera");
        this.videoCapture.release();
        
    }
}
