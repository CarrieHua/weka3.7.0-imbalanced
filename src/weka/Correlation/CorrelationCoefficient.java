package weka.Correlation;



import weka.MyMath.MyMath;
import weka.MyMath.*;


public class CorrelationCoefficient {
    public static void main(String argv[]){
        System.out.println("In the main of CorrelationCoefficient");
//        double []arra= {1,2,3,4,5,6,7};
//        double []arrb = {1,7,2,6,3,5,4};
//        String []arrA = {"wang","chao","wang","chao","wang","chao","wang","chao"};
//        String []arrB = {"wang","chao","hello","world","once","once","nihao","wohao"};
//        System.out.println(""+CorrelationCoefficient.ChiSquareCoefficient(arrA, arrB));
//        for (int i = 0; i < arrA.length; i++) {
//            System.out.println(""+arrA[i]+","+arrB[i]+","+arrA[i]);
//        }

        double [][] values ={
            {103.0, 103.0, 103.0,},
        { 104.3,  104.3,        104.3,} ,
{188.8,        188.8,        188.8,  } ,
{67.2,        67.2,        67.2,   } ,
{56.2,        56.2,        56.2,   } ,
{3045.0,        2912.0,        2935.0,} ,
{130.0,        141.0,        141.0,} ,
{3.62,        3.78,        3.78,  } ,
{3.15,        3.15,        3.15,  } ,
{7.5,        9.5,        9.5,     } ,
{162.0,        114.0,        114.0, } ,
{5100.0,        5400.0,        5400.0, } ,
{17.0,        23.0,        24.0,   } ,
{22.0,        28.0,        28.0,  } ,
{18420.0,        12940.0,        15985.0, } ,
        };

        
//        for (int i = 0; i < values.length-1; i++) {
//            for (int j = i+1; j < values.length; j++) {
//                 double result = CorrelationCoefficient.PearsonProductMomentCoefficient(values[i], values[j]);
//                  System.out.println("i="+i+",j="+j+":"+result);
//            }
//
//        }

         double arra[] = {56.2,        56.2,        56.2,   } ;
        double arrb[] = {188.8,        188.8,        188.8,  };
        double c[] = {1,2,2,3,6,9};
        double d[] = {1,1,1,1,1,1};
        double result = CorrelationCoefficient.PearsonProductMomentCoefficient(c, d);
        System.out.println(""+result);


    }



    public static double PearsonProductMomentCoefficient(double []arrA,double []arrB){
        double result  = 0.0;
        double Molecular = 0.0;
        double Denominator= 0.0;
        int N = arrA.length;
        double meanA = MyMath.mean(arrA);

        double meanB = MyMath.mean(arrB);        
        double stdA = MyMath.std(arrA);
        double stdB = MyMath.std(arrB);
        if(MyMath.EqualZero(stdA)||MyMath.EqualZero(stdB)||N<2){
            return 0.0;
        }
        for (int i = 0; i < N; i++) {
            Molecular += arrA[i]*arrB[i];
        }
        Molecular -= N*meanA*meanB;

        Denominator = (N-1)*stdA*stdB;
        result = Molecular/Denominator;
        return result;
    }

//    public static double ChiSquareCoefficient(String []arrA,String[]arrB){
//        double similarity = 0.0;
//        StrAttriStat arrAStat = new StrAttriStat(arrA);
//        StrAttriStat arrBStat = new StrAttriStat(arrB);
//        double contingency [][] = getContingencyTable(arrA,arrAStat,arrB, arrBStat);
////        MyArrayDealer.showArray(contingency);
//        double chiSquare = getChiSquare(contingency);
//        double freeA = arrAStat.distCount-1;
//        double freeB = arrBStat.distCount-1;
//        if(freeA*freeB==0){
//            similarity = 0;
//        }else{
//             similarity = chi2cdf(chiSquare,freeA*freeB);
//        }       
//        return similarity;
//    }
//
//   
//
//    private static double getChiSquare(double[][] contingency) {
//        double result = 0.0;
//        double N = contingency[contingency.length-1][contingency[0].length-1];
//        for (int i = 0; i < contingency.length; i++) {
//            for (int j = 0; j < contingency[i].length; j++) {
//                double obserValue = contingency[i][j];
//                double contA = contingency[i][contingency[i].length-1];
//                double contB = contingency[contingency.length-1][j];
//                double expecValue = contA*contB/N;
//                result+= Math.pow((obserValue-expecValue), 2)/expecValue;
//            }
//        }
//        return result;
//    }
//
//    private static double[][] getContingencyTable(String[] arrA, StrAttriStat arrAStat, String[] arrB, StrAttriStat arrBStat) {
//        double [][] conTable = new double[arrAStat.distCount+1][arrBStat.distCount+1];
//        for (int i = 0; i < conTable.length; i++) {
//            for (int j = 0; j < conTable[i].length; j++) {
//                conTable[i][j] = 0.0;
//            }
//        }
//        for (int i = 0; i < arrA.length; i++) {
//            String obserA = arrA[i];
//            String obserB = arrB[i];
//            int indexA = arrAStat.getStringIndex(obserA);
//            int indexB = arrBStat.getStringIndex(obserB);
//            conTable[indexA][indexB] +=1;
//            conTable[indexA][arrBStat.distCount]+=1;
//            conTable[arrAStat.distCount][indexB]+=1;
//        }
//        conTable[arrAStat.distCount][arrBStat.distCount] = arrA.length;
//        return conTable;
//    }
//
//    private static double chi2cdf(double chiSquare, double d) {
//        Stat s = new Stat();
//        return Stat.chiSquareCDF(chiSquare, (int)d);
//    }
//
//    private static class StrAttriStat {
//        int distCount;
//        String contentStrings[];
//        int    contentStringsOccueredTimes[];
//        public StrAttriStat(String []array) {
//            StringInfor[] strInfor = getStringInfor(array);
//            this.distCount = strInfor.length;
//            this.contentStrings = new String[this.distCount];
//            this.contentStringsOccueredTimes = new int[this.distCount];
//            for (int i = 0; i < strInfor.length; i++) {
//                this.contentStrings[i] = strInfor[i].getKey();
//                this.contentStringsOccueredTimes[i] = strInfor[i].getCount();
//            }
//        }
//        public int getStringIndex(String aim){
//            return  Arrays.binarySearch(contentStrings, aim);
//        }
//        public int getStringOccuredTimes(String aim){
//            int index = this.getStringIndex(aim);
//            if(index != -1){
//                return contentStringsOccueredTimes[index];
//            }else{
//                return -1;
//            }
//        }
//
//        private StringInfor[] getStringInfor(String[] array) {
//           StringInfor tempInfor[] =  new StringInfor[array.length];
//           int strInforLen = 0;
//           
//            for (int i = 0; i < array.length; i++) {
//               String key =array[i];
//               int index = -1;
//               if((index = isKeyInInforArray(key,tempInfor,strInforLen))!=-1){
//                   tempInfor[index].countOneMore();
//               }else{
//                   tempInfor[strInforLen] = new StringInfor(key);
//                   strInforLen++;
//               }                
//            }
//           StringInfor stringInfor[] = new StringInfor[strInforLen];
//            for (int i = 0; i < strInforLen; i++) {
//                stringInfor[i] = new StringInfor(tempInfor[i]);
//            }
//           Arrays.sort(stringInfor, new StringInforComparer());
//           return stringInfor;
//        }
//
//        private int isKeyInInforArray(String key, StringInfor[] tempInfor, int strInforLen) {
//            for (int i = 0; i < strInforLen; i++) {
//                if(tempInfor[i].getKey().equals(key)){
//                    return i;
//                }
//            }
//            return -1;
//        }
//    }
//
//    private static class StringInfor {
//       private String key;
//       private int count;
//
//        private StringInfor(StringInfor stringInfor) {
//            this.key = stringInfor.getKey();
//            this.count = stringInfor.getCount();
//        }
//
//        public int getCount() {
//            return count;
//        }
//
//        public String getKey() {
//            return key;
//        }
//        public StringInfor(String key) {
//            this.key = key;
//            count = 1;
//        }
//        public void countOneMore(){
//            count++;
//        }
//    }
//
//    private static class StringInforComparer implements Comparator{
//
//        public StringInforComparer() {
//        }
//
//        public int compare(Object o1, Object o2) {
//            String o1key = ((StringInfor)o1).getKey();
//            String o2key = ((StringInfor)o2).getKey();
//            return o1key.compareTo(o2key);
//        }
//    }
}
