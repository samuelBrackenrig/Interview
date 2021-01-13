import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class Test extends ApplicationFrame {

    private static final String TITLE = "Longwall Interface";
    private static final String START = "Start";
    private static final String STOP = "Stop";
    private static final int FAST = 1;
    private static final int REALTIME = 1000;
    private static final Random random = new Random();
    private static final double threshold = 35.64;
    private double gateStart = ThreadLocalRandom.current().nextInt(0, 101);
    private boolean returning = false;
    private boolean offline = false;
    private boolean waiting = false;
    private boolean paused = false;
    private Timer timer;
    private Calendar startDate;
    private Calendar datasetTime = Calendar.getInstance();
    private final TimeSeries series = new TimeSeries("Longwall Data");

    public Test(final String title) throws ParseException {
        super(title);

        SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy HH:mm", Locale.ENGLISH);
        PriceParser parser = new PriceParser();
        List<List<String>> priceData = parser.parse();
        Date date = formatter.parse(priceData.get(0).get(0));
        startDate = Calendar.getInstance();
        startDate.setTime(date);
        datasetTime.setTime(date);

        final TimeSeriesCollection dataset = new TimeSeriesCollection();
        dataset.addSeries(this.series);

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
        JLabel price = new JLabel("Price threshold exceeded, operations ceasing.");

        this.add(new ChartPanel(chart), BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(combo);
        JPanel test = new JPanel();
        test.add(label);
        this.add(btnPanel, BorderLayout.SOUTH);

        timer = new Timer(FAST, new ActionListener() {
            Date timeToCheck = formatter.parse(priceData.get(0).get(0));
            Calendar pauseResume = Calendar.getInstance();
            Calendar offlineTime = Calendar.getInstance();
            double currentPrice;
            float[] newData = new float[1];
            PopupFactory pf = PopupFactory.getSharedInstance();
            Popup pricePopup;
            Popup offlinePopup;

            @Override
            public void actionPerformed(ActionEvent e) {

                if(offline){
                    if(offlineTime.getTime().compareTo(datasetTime.getTime()) == 0){
                        offline = false;
                        offlinePopup.hide();
                    }
                }

                if(ThreadLocalRandom.current().nextInt(0, 1001) > 999 && !offline){
                    offline = true;
                    offlineTime.setTime(datasetTime.getTime());
                    offlineTime.add(Calendar.SECOND, ThreadLocalRandom.current().nextInt(1, 5)*10);
                    series.addOrUpdate(new Second(datasetTime.getTime()), null);
                    offlinePopup = pf.getPopup(btnPanel, label, 900, 300);
                    offlinePopup.show();
                }

                if(timeToCheck.compareTo(datasetTime.getTime()) == 0){
                    currentPrice = Double.valueOf(priceData.get(0).get(1));
                    paused = currentPrice >= threshold;
                    if(paused){
                        pricePopup = pf.getPopup(btnPanel, price, 850, 280);
                        pricePopup.show();
                    }else if(!paused && waiting){
                        pauseResume.setTime(datasetTime.getTime());
                        pauseResume.add(Calendar.SECOND, 120);
                    }
                    priceData.remove(0);
                    try {
                        timeToCheck = formatter.parse(priceData.get(0).get(0));
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                }

                if(!paused){
                    if(!waiting){
                        if(Math.round(gateStart) * 10 / 10.0 == 100d){
                            returning = true;
                        } else if (Math.round(gateStart) * 10 / 10.0 == 0){
                            returning = false;
                        }
                        if(returning){
                            gateStart -= 0.1d;
                        } else {
                            gateStart += 0.1d;
                        }
                    }else{
                        if(datasetTime.getTime().compareTo(pauseResume.getTime()) == 0){
                            waiting = false;
                            pricePopup.hide();
                        }
                    }
                }else{
                    if(Math.round(gateStart)*10/10.0 == 0 || Math.round(gateStart)*10/10.0 == 100){
                        waiting = true;
                    }else{
                        if(Math.round(gateStart)*10/10.0 >= 50){
                            gateStart += 0.1d;
                        }else if(Math.round(gateStart)*10/10.0 < 50){
                            gateStart -= 0.1d;
                        }
                    }
                }
                newData[0] = (float)gateStart;
                datasetTime.add(Calendar.SECOND, 1);
                if(!offline){
                    series.addOrUpdate(new Second(datasetTime.getTime()), gateStart);
                }
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
            if(waiting || paused){
                return super.getItemPaint(row, col);
            }else{
                if(!offline){
                    return new Color(0, 255, 0);
                }else{
                    return new Color(0, 0, 0);
                }
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
                Test demo = null; //pass date in from csv
                try {
                    demo = new Test(TITLE);
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