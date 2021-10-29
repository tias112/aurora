package aurora.mag;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.analysis.interpolation.DividedDifferenceInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunctionNewtonForm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

public class TestDateApplication {


    public static void main(String[] args) {
        double[] x = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269};
        double[] y = {-1574.4, -1572.5, -1571, -1570.2, -1570.1, -1570.6, -1571, -1571.3, -1571.9, -1572.7, -1573.1, -1573.4, -1573, -1572.4, -1571.6, -1571.7, -1571.2, -1570, -1569.1, -1568.8, -1569.2, -1570.9, -1573.2, -1576.1, -1579.7, -1583.5, -1587.5, -1591.4, -1595.9, -1600.5, -1604.2, -1607.7, -1610.5, -1613.9, -1618.4, -1622.2, -1626, -1631, -1636.9, -1642.9, -1649.8, -1656.6, -1662.7, -1668.6, -1674.8, -1681.1, -1687.8, -1694.7, -1701.6, -1708.9, -1714.6, -1722, -1728, -1732.4, -1736.6, -1740.6, -1743.9, -1746.6, -1747.8, -1749.2, -1749.4, -1749.6, -1750.3, -1751, -1752.3, -1753.6, -1755, -1756.3, -1757.6, -1759, -1760, -1761.4, -1762.7, -1763.1, -1763.7, -1764.3, -1764.3, -1764.6, -1764.5, -1764.4, -1763.7, -1763.7, -1764.1, -1764, -1764.2, -1764.7, -1765.5, -1766.4, -1768.4, -1769.7, -1771.7, -1773.8, -1775.6, -1777.6, -1780.3, -1782.4, -1785.5, -1788, -1790.6, -1793, -1795, -1797.3, -1800.1, -1802.5, -1804.8, -1807.5, -1810.6, -1813.7, -1816.6, -1820, -1821.7, -1824.5, -1828.1, -1831.4, -1835.3, -1839.1, -1843.3, -1847.4, -1851.9, -1856, -1859.5, -1863.3, -1867.6, -1871.9, -1875.6, -1879.4, -1883.6, -1887.5, -1890.5, -1893, -1895.6, -1898, -1900.5, -1902.8, -1904.9, -1906.7, -1908.4, -1909.9, -1911.8, -1914.7, -1917.5, -1920.1, -1922.8, -1925.8, -1929.5, -1933.2, -1936.7, -1940.7, -1944.4, -1948, -1950.4, -1952.7, -1954.5, -1956.6, -1958.8, -1959.8, -1960.4, -1961.6, -1964, -1966.5, -1968.7, -1970.7, -1972.6, -1974.5, -1976.6, -1977.9, -1979.5, -1980.7, -1981.5, -1982, -1982.2, -1982.8, -1982.4, -1981.5, -1980, -1979.2, -1979.5, -1980, -1980.6, -1981.8, -1983.8, -1985.8, -1986.8, -1987.2, -1988.3, -1990.3, -1992.3, -1993.9, -1994.7, -1994.3, -1995.5, -1997.1, -1998, -1999.1, -1999.9, -2002, -2003.6, -2005, -2004.7, -2003.1, -2001.3, -1999, -1996.5, -1993.5, -1991.4, -1989.7, -1988.7, -1988.5, -1988.6, -1988.3, -1987.4, -1986.5, -1986.1, -1986.2, -1987.4, -1989.4, -1991.7, -1993.8, -1997.1, -2000, -2002.8, -2004.8, -2006.4, -2008.1, -2010.8, -2012.7, -2013.8, -2014.2, -2012.9, -2011.9, -2010.8, -2008.9, -2008, -2006.9, -2005.5, -2003.4, -2001.8, -2000.5, -1999.2, -1998.1, -1997, -1996.6, -1995.5, -1995.3, -1995.8, -1996.5, -1997, -1997.5, -1998.1, -1999.1, -1999.4, -1999.7, -1999.8, -1999.2, -1998.1, -1996.5, -1995.1, -1993.9, -1992.8, -1991.4, -1989.1, -1986.8, -1983.8, -1981.9, -1980.9, -1979.2, -1975.9, -1973.6, -1972.7};

        DividedDifferenceInterpolator divider = new DividedDifferenceInterpolator();
        PolynomialFunctionNewtonForm polynom = divider.interpolate(x, y);
        double[] coefficients = polynom.getCoefficients();
        System.out.println(" " + coefficients.length + " " + Arrays.toString(coefficients));

        PolynomialFunction derivative =
                (PolynomialFunction) new PolynomialFunction(coefficients).derivative();
        derivative.value(1);
        System.out.println(Arrays.toString(derivative.getCoefficients()));
        System.out.println(derivative.value(1));
        System.out.println(derivative.value(2));
        System.out.println(derivative.value(3));
        System.out.println(derivative.value(4));
        System.out.println(derivative.value(5));
        System.out.println(derivative.value(6));
    }

    private void testNet() {
        try {
            URL url = new URL("https://www2.irf.se/maggraphs/rt.txt");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Range", "Bytes=-100");
            urlConnection.connect();

            System.out.println("Respnse Code: " + urlConnection.getResponseCode());
            System.out.println("Content-Length: " + urlConnection.getContentLengthLong());

            InputStream inputStream = urlConnection.getInputStream();
            long size = 0;

            FileUtils.copyInputStreamToFile(inputStream, new File("C:\\temp\\1.txt"));
/*
            while(inputStream.read() != -1 )
                size++;
*/

            System.out.println("Downloaded Size: " + size);

        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }


}
