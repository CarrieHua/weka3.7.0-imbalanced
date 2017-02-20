package weka;

public class test {

	/**
	 * @param args
	 */
	public static void njjMP(int a[]){
		int temp;
		int njjlen=a.length;
		boolean tag=true;
		while(tag){
			tag=false;
			njjlen--;
			for (int j=1; j <=njjlen; j++ ){
				if(a[j-1] < a[j]){
					temp = a[j-1];
					a[j-1] =a[j];
					a[j] = temp;
					tag=true;
				}
			}
		}
	}

	public static void main(String[] args) {
		int a[]={4,5,6,7,0,1,2,3,8,9};
		for(int i=0;i<10;i++)
			System.out.print("*"+a[i]);
			System.out.println();
		njjMP(a);
		for(int i=0;i<10;i++)
			System.out.print("--"+a[i]);
			System.out.println();
		// TODO Auto-generated method stub
//      System.out.println("njj");
	}

}
