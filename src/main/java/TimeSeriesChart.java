import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TimeSeriesChart extends JPanel {

    private final DynamicTimeSeriesCollection dataset;
    private final JFreeChart chart;


    public TimeSeriesChart(final String title){
        dataset = new DynamicTimeSeriesCollection(1, 1000, new Second());
        dataset.setTimeBase(new Second(0, 0, 0, 1, 1, 2021));
        dataset.addSeries(new float[1], 0, title);
        chart = ChartFactory.createTimeSeriesChart(
                title, "Time", title, dataset, true, true, false);
        final XYPlot plot = chart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setFixedAutoRange(10000);
        axis.setDateFormatOverride(new SimpleDateFormat("hh.HH"));
        final ChartPanel chartPanel = new ChartPanel(chart);
        add(chartPanel);
    }

    public void update(float value) {
        float[] newData = new float[1];
        newData[0] = value;
        dataset.advanceTime();
        dataset.appendData(newData);
    }

    public XYDataset createDataset(){
        Random random = new Random();
        int randomStartH = random.nextInt(12);
        int randomEndH = ThreadLocalRandom.current().nextInt(randomStartH+2, 24+1);
        System.out.println(randomStartH);
        System.out.println(randomEndH);

        int randomStartM = random.nextInt(60);
        int randomEndM = random.nextInt(60);

        double gateStart = ThreadLocalRandom.current().nextInt(0, 101);

        final TimeSeries timeSeries = new TimeSeries("Shearer Data");
        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        final Day day = new Day();

        final Hour endHour = new Hour(randomEndH, day);
        boolean returning = false;
        for(int i = randomStartH; i < randomEndH; i++){
            final Hour currentHour = new Hour(i, day);
            for(int j = 0; j < 60; j++){
                System.out.println(gateStart);
                timeSeries.add(new Minute(j, currentHour), gateStart);
                if(gateStart == 100){
                    returning = true;
                }else if(gateStart == 0){
                    returning = false;
                }
                if(returning){
                    gateStart--;
                }else{
                    gateStart++;
                }
            }
        }
        dataset.addSeries(timeSeries);
        return dataset;
    }

}
