package aurora.mag.batch.log;

import aurora.mag.batch.MagRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.springframework.batch.item.ItemWriter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MagItemWriter implements ItemWriter<MagRecord> {
    DividedDifferenceInterpolator divider = new DividedDifferenceInterpolator();
    PolynomialFunction derivative;
    final static public int Q_PAGE_SIZE = 15 * 60;
    public static Map<String, Queue<Float>> pageQueues = new HashMap<>() {{
        this.put("X", new PagedMetric());
        this.put("Y", new PagedMetric());
        this.put("Z", new PagedMetric());
    }};


    public static boolean isBetween(float x, float lower, float upper) {
        return lower <= x && x <= upper;
    }

    private Queue<Float> getComponentXPage() {
        return pageQueues.get("X");
    }

    private Queue<Float> getComponentYPage() {
        return pageQueues.get("Y");
    }

    private Queue<Float> getComponentZPage() {
        return pageQueues.get("Z");
    }

    private Float getMinMaxDelta(Queue<Float> pageQueue) {
        Float min = pageQueue.stream().min(Comparator.comparing(Float::valueOf)).orElse(0.0f);
        Float max = pageQueue.stream().max(Comparator.comparing(Float::valueOf)).orElse(0.0f);
        return max - min;
    }

    @Override
    public void write(List<? extends MagRecord> list) throws Exception {
        // calculateDeriviative(list.stream().map(MagRecord::getXcomponent).map(Float::doubleValue).collect(Collectors.toList()));
        for (MagRecord magRecord : list) {
            if (magRecord.getXcomponent() != null) {
                getComponentXPage().add(magRecord.getXcomponent());
            }
            if (magRecord.getYcomponent() != null) {
                getComponentYPage().add(magRecord.getYcomponent());
            }
            if (magRecord.getZcomponent() != null) {
                getComponentZPage().add(magRecord.getZcomponent());
            }
        }


        String q = decideQ(pageQueues.values().stream()
                .map(this::getMinMaxDelta)
                .max(Float::compareTo).orElse(0.0f));
        for (MagRecord magRecord : list) {
            String timestamp = magRecord.getTimestamp();
            // magRecord.setDeriviativeX((float) derivative.value(list.indexOf(magRecord)));
            log.info(timestamp + " " + magRecord.getXcomponent()
                    + " " + magRecord.getYcomponent()
                    + " " + magRecord.getZcomponent()
                    + " " + getMinMaxDelta(getComponentXPage())//(Float.isInfinite(magRecord.getDeriviativeX()) ? 1000.0 : Math.abs(magRecord.getDeriviativeX()))
                    + " " + q
            );
        }

        Thread.sleep(300);
    }

    private String decideQ(Float num) {

        if (isBetween(num, 0, 15)) {
            return "0";
        } else if (isBetween(num, 15, 30)) {
            return "1";
        } else if (isBetween(num, 30, 60)) {
            return "2";
        } else if (isBetween(num, 60, 120)) {
            return "3";
        } else if (isBetween(num, 120, 210)) {
            return "4";
        } else if (isBetween(num, 210, 360)) {
            return "5";
        } else if (isBetween(num, 360, 600)) {
            return "6";
        } else if (isBetween(num, 600, 990)) {
            return "7";
        } else if (isBetween(num, 990, 1500)) {
            return "8";
        } else if (num > 1500) {
            return "9";
        }
        return "0";
    }

    private void calculateDeriviative(List<Double> collect) {
        Double[] x = IntStream.rangeClosed(0, collect.size() - 1).boxed().map(Integer::doubleValue).collect(Collectors.toList()).toArray(new Double[collect.size()]);
        Double[] y = collect.toArray(new Double[collect.size()]);
        PolynomialFunctionNewtonForm polynom = divider.interpolate(ArrayUtils.toPrimitive(x),
                ArrayUtils.toPrimitive(y));
        double[] coefficients = polynom.getCoefficients();
        derivative =
                (PolynomialFunction) new PolynomialFunction(coefficients).derivative();

    }

    private static class PagedMetric extends LinkedList<Float> {

        private static final long serialVersionUID = -6707803882461262867L;

        @Override
        public boolean add(Float object) {
            boolean result;
            if (this.size() < Q_PAGE_SIZE)
                result = super.add(object);
            else {
                super.removeFirst();
                result = super.add(object);
            }
            return result;
        }
    }

}
