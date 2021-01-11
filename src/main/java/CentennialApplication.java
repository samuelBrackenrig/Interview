import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import org.jfree.data.xy.XYSeries;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CentennialApplication extends Application {

    //public static void main(String[] args) {
    ScheduledExecutorService scheduledExecutorService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JavaFX Realtime Chart Demo");
//        GUI gui = new GUI();
//        TimeSeriesChart chart = new TimeSeriesChart("Chart");
//        chart.pack();
//        Refinery

//        SwingUtilities.invokeLater(() -> {
//            TimeSeriesChart example = new TimeSeriesChart("Time Series Chart");
//            example.setSize(800, 400);
//            example.setLocationRelativeTo(null);
//            example.setVisible(true);
//            example.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        });
        final CategoryAxis xAxis = new CategoryAxis(); // we are gonna plot against time
        final NumberAxis yAxis = new NumberAxis(0.0, 100.0, 1.0);
        xAxis.setLabel("Time/s");
        xAxis.setAnimated(false); // axis animations are removed
        yAxis.setLabel("Shearer Position");
        yAxis.setTickLabelsVisible(false);
        yAxis.setAnimated(false); // axis animations are removed

        //creating the line chart with two axis created above
        final LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Realtime JavaFX Charts");
        lineChart.setAnimated(false); // disable animations

        //defining a series to display data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Data Series");

        // add series to chart
        lineChart.getData().add(series);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        final int WINDOW_SIZE = 10;

        final int[] gateStart = {ThreadLocalRandom.current().nextInt(0, 101)};

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);

        // show the stage
        primaryStage.show();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {

            final boolean[] returning = {false};

            // Update the chart
            Platform.runLater(() -> {
                // get current time
                Date now = new Date();
                // put random number with current time
                series.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), gateStart[0]));
                System.out.println(gateStart[0]);
                if(gateStart[0] == 100){
                    returning[0] = true;
                }else if(gateStart[0] == 0){
                    returning[0] = false;
                }
                if(returning[0]){
                    gateStart[0]--;
                }else{
                    gateStart[0]++;
                }

                if (series.getData().size() > WINDOW_SIZE)
                    series.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        scheduledExecutorService.shutdownNow();
    }

}
