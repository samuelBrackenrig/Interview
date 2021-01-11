import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.*;
import javax.swing.Timer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class DTSCTest extends ApplicationFrame {

    private static final String TITLE = "Dynamic Series";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final int COUNT = 1000*60;
    private static final int FAST = 1; //1000/FAST = occurrences per second real time
    private static final int REALTIME = FAST * 1000;
    private static final Random random = new Random();
    private static final double threshold = 35;
    private double gateStart = ThreadLocalRandom.current().nextInt(0, 101);
    private boolean returning = false;
    private boolean offline = false;
    private Timer timer;
    private Calendar startDate;
    private static final int simulationSpeed = 1000/FAST;

    public DTSCTest(final String title) throws ParseException {
        super(title);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm", Locale.ENGLISH);
        PriceParser parser = new PriceParser();
        List<List<String>> priceData = parser.parse();
        Date date = formatter.parse(priceData.get(0).get(0));
        startDate = Calendar.getInstance();
        startDate.setTime(date);
        Calendar timeBaseStartDate = Calendar.getInstance();
        timeBaseStartDate.setTime(startDate.getTime());
        timeBaseStartDate.add(Calendar.SECOND, -COUNT);

        final DynamicTimeSeriesCollection dataset =
                new DynamicTimeSeriesCollection(1, COUNT, new Second());
        dataset.setTimeBase(new Second(timeBaseStartDate.getTime()));
        dataset.addSeries(new float[1], 0, "Longwall Data");
        dataset.addValue(0, 0, (float) gateStart);

        JFreeChart chart = createChart(dataset);
        final JComboBox combo = new JComboBox();
        combo.addItem("Fast");
        combo.addItem("Real-time");
        combo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("Fast".equals(combo.getSelectedItem())) {
                    timer.setDelay(FAST);
                } else {
                    timer.setDelay(REALTIME);
                }
            }
        });

        JFrame frame = new JFrame("Test");
        JLabel label = new JLabel("Network connectivity lost.");

        this.add(new ChartPanel(chart), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(combo);
        JPanel test = new JPanel();
        test.add(label);
        this.add(btnPanel, BorderLayout.SOUTH);

       // frame.add(btnPanel);
        //frame.add(test);

        timer = new Timer(FAST, new ActionListener() {
            Date timeToCheck = formatter.parse(priceData.get(0).get(0));
            Calendar pauseResume = Calendar.getInstance();
            Calendar offlineTime = Calendar.getInstance();
            boolean paused = false;
            boolean waiting = false;
            //boolean offline = false;
            double currentPrice;
            float[] newData = new float[1];
            PopupFactory pf = PopupFactory.getSharedInstance();
            Popup popup;

            @Override
            public void actionPerformed(ActionEvent e) {
                Date datasetTime = dataset.getNewestTime().getStart();
                if(offline){
                    System.out.println("Offline: "+offlineTime.getTime());
                    System.out.println("Current: "+datasetTime);
                    if(offlineTime.getTime().compareTo(datasetTime) == 0){
                        offline = false;
                        System.out.println("Im no longer offline");
                        popup.hide();
                    }
                }

                if(ThreadLocalRandom.current().nextInt(0, 1001) > 999 && !offline){
                    offline = true;
                    offlineTime.setTime(datasetTime);
                    offlineTime.add(Calendar.SECOND, ThreadLocalRandom.current().nextInt(1, 5)*10);

//                    dataset.addValue(0, 0, null);
                    popup = pf.getPopup(btnPanel, label, 900, 300);
                    popup.show();
                }

                if(timeToCheck.compareTo(datasetTime) == 0){
                    currentPrice = Double.valueOf(priceData.get(0).get(1));
                    paused = currentPrice >= threshold;
                    priceData.remove(0);
                    try {
                        timeToCheck = formatter.parse(priceData.get(0).get(0));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                if(!paused) {
                    if (Math.round(gateStart) * 10 / 10.0 == 100d) {
                        returning = true;
                    } else if (Math.round(gateStart) * 10 / 10.0 == 0) {
                        returning = false;
                    }
                    if (returning) {
                        gateStart -= 0.1d;
                    } else {
                        gateStart += 0.1d;
                    }
                }else{
                    if(datasetTime.compareTo(pauseResume.getTime()) == 0 && currentPrice < threshold){
                        paused = false;
                        waiting = false;
                    }else{
                        if(Math.round(gateStart)*10/10.0 == 0 || Math.round(gateStart)*10/10.0 == 100){
                            if(!waiting){
                                pauseResume.setTime(datasetTime);
                                pauseResume.add(Calendar.SECOND, 120);
                            }
                            waiting = true;
                        }else{
                            if(Math.round(gateStart)*10/10.0 >= 50){
                                gateStart += 0.1d;
                            }else if(Math.round(gateStart)*10/10.0 < 50){
                                gateStart -= 0.1d;
                            }
                        }
                    }
                }
                newData[0] = (float)gateStart;
                dataset.advanceTime();
                dataset.appendData(newData);
            }
        });
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final JFreeChart result = ChartFactory.createTimeSeriesChart(
                TITLE, "Time", "Shearer Position", dataset, true, true, false);
        final XYPlot plot = result.getXYPlot();
        plot.setDomainZeroBaselineVisible(false);
        XYLineAndShapeRendererTest renderer = new XYLineAndShapeRendererTest(true, false);
        plot.setRenderer(renderer);
        DateAxis domain = (DateAxis)plot.getDomainAxis();

        Calendar endDate = Calendar.getInstance();
        endDate.setTime(startDate.getTime());
        endDate.add(Calendar.HOUR_OF_DAY, 12);

        domain.setRange(startDate.getTime(), endDate.getTime());
        domain.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
        domain.setDateFormatOverride(new SimpleDateFormat("HH:mm"));

        ValueAxis range = plot.getRangeAxis();
        range.setRange(0, 100);

        return result;
    }

    private class XYLineAndShapeRendererTest extends XYLineAndShapeRenderer {

        private boolean drawSeriesLineAsPath;

        public XYLineAndShapeRendererTest(boolean line, boolean shapes){
            super(line, shapes);
        }

        @Override
        public Paint getItemPaint(int row, int col) {
            if(!offline){
                return super.getItemPaint(row, col);
            }else{
                return new Color(0, 0, 0);
            }

        }
    }

    private void start() {
        timer.start();
    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                DTSCTest demo = null; //pass date in from csv
                try {
                    demo = new DTSCTest(TITLE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                demo.pack();
                RefineryUtilities.centerFrameOnScreen(demo);
                demo.setVisible(true);
                demo.start();
            }
        });
    }
}