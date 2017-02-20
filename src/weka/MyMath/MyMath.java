/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.MyMath;

//import MyArraySmallTools.MyArrayDealer;
import java.util.Arrays;



public class MyMath {

    public static double sum(double[] arrays) {
        double result = 0.0;
        for (int i = 0; i < arrays.length; i++) {
            result += arrays[i];
        }
        return result;
    }

    public static double mean(double[] arrays) {
        double result = 0.0;
        if (arrays.length == 0) {
            return 0;
        }
        result = MyMath.sum(arrays);
        return result / arrays.length;
    }

    public static double var(double[] arrays) {
        double result = 0.0;
        if (arrays.length == 0) {
            return 0;
        }
        double mean = MyMath.mean(arrays);
        for (int idx = 0; idx < arrays.length; idx++) {
            result += Math.pow(arrays[idx] - mean, 2);
        }
        return result / (arrays.length - 1);
    }

    public static double std(double[] arrays) {
        return Math.pow(MyMath.var(arrays), 0.5);
    }

    public static double prctile(double[] values, double d) {
        int N = values.length;
        if(d <0 || d>100){
            return 0.0/0;
        }
        Arrays.sort(values);
        double prectile[] = getPrectileOf(N);  
        if(d<=prectile[0]){
            return values[0];
        }
        if(d>=prectile[prectile.length-1]){
            return values[values.length-1];
        }
        int end  = 0;
        for (int i = 0; i < prectile.length; i++) {
            if(d>prectile[i]){
                end++;
            }else{
                break;
            }
        }
        double result = 0.0;
        result = values[end-1]+(d-prectile[end-1])*(values[end]-values[end-1])/(prectile[end]-prectile[end-1]);
        return result;
    }

    public static double trimmean(double[] values, double p) {
        Arrays.sort(values);
        double max = MyMath.max(values);
        double min = MyMath.min(values);
        if(max == min){
            return min;
        }

        double zlow = MyMath.prctile(values, p / 2);
        double zhi = MyMath.prctile(values, 100.0 - (p / 2));
        int counter = 0;
        double sum = 0.0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] >= zlow && values[i] <= zhi) {
                sum += values[i];
                counter++;
            }
        }
        return sum / counter;
    }

    public static double min(double[] values) {
        Arrays.sort(values);
        return values[0];
    }

    public static double max(double[] values) {
        Arrays.sort(values);
        return values[values.length - 1];
    }

    private static double[] getPrectileOf(int N) {
        double result[] = range(0.5,N-0.5,1);   
        for (int i = 0; i < result.length; i++) {
              result[i]*=100.0/N;
        }
        return result;
    }

    public  static double[] range(double d, double d0, int i) {
       double result[] = new double[(int)(d0-d+1)];
       result[0] = d;
        for (int j = 1; j < result.length; j++) {
            result[j] = result[j-1]+i;
        }
       return result;
    }

    public static double meanWithWeight(double[] values, double[] weights) {
        double result = 0.0;
        for (int i = 0; i < weights.length; i++) {
            result += weights[i]*values[i];
        }
        return result/MyMath.sum(weights);
    }
        public static boolean EqualZero(double d){
        double precision = 0.0000000001;
        if( Math.abs(d-0.0)<precision){
            return true;
        }else{
            return false;
        }
    }
}
