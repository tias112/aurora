package aurora.mag.batch.log;

import aurora.mag.batch.MagRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;
import org.springframework.batch.item.ItemWriter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class MagItemWriter implements ItemWriter<MagRecord> {
    DividedDifferenceInterpolator divider = new DividedDifferenceInterpolator();
    PolynomialFunction derivative;

    @Override
    public void write(List<? extends MagRecord> list) throws Exception {
        calculateDeriviative(list.stream().map(MagRecord::getXcomponent).map(Float::doubleValue).collect(Collectors.toList()));
        for (MagRecord regionsDTO : list) {
            String timestamp = regionsDTO.getTimestamp();
            regionsDTO.setDeriviativeX((float) derivative.value(list.indexOf(regionsDTO)));
            log.info(timestamp + " " + regionsDTO.getXcomponent()
                    + " " + regionsDTO.getYcomponent()
                    + " " + regionsDTO.getZcomponent()
                    + " " + (Float.isInfinite(regionsDTO.getDeriviativeX()) ? 1000.0 : Math.abs(regionsDTO.getDeriviativeX()))
            );
        }

        Thread.sleep(300);
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
}
