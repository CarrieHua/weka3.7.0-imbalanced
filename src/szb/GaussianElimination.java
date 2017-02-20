package szb;

import java.util.Scanner;

public class GaussianElimination {

	/**
	 * @列主元高斯消去法
	 */
	public double a[][];
	public double b[];
	public double x[];
	public int n;
	public int n2; //记录换行的次数
	
	public GaussianElimination(int k)
	{
		 n=k;
		 a=new double[n+1][n+1];
	     b=new double[n+1];
	     x=new double[n+1];
	}
	
    public void Elimination(){  //消元
		for(int k=1;k<=n-1;k++)
		{
			Wrap(k);
			for(int i=k+1;i<=n;i++)
			{
				double l=a[i][k]/a[k][k];
				a[i][k]=0.0;
				for(int j=k+1;j<=n;j++)
					a[i][j]=a[i][j]-l*a[k][j];
				b[i]=b[i]-l*b[k];
			}
			//System.out.println("第"+k+"次消元后：");
			//PrintA();
		}
				
	}
    public void Back()//回代
    {
    	x[n]=b[n]/a[n][n];
    	for(int i=n-1;i>=1;i--)
    		x[i]=(b[i]-jisuan(i))/a[i][i];
    }
    public double jisuan(int i){
    	double he=0.0;
    	for(int j=i+1;j<=n;j++)
    		he=he+x[j]*a[i][j];
    	return he;
    }
    public void Wrap(int k){//换行
    	double max=Math.abs(a[k][k]);
    	int n1=k;                   //记住要交换的行
    	for(int i=k+1;i<=n;i++)     //找到要交换的行
    	{
    		if(Math.abs(a[i][k])>max){
    			n1=i;
    			max=Math.abs(a[i][k]);
    		}
    	}
    	if(n1!=k)
    	{
    		n2++;
    	//System.out.println("当k="+k+"时,要交换的行是："+k+"和"+n1);
    	for(int j=k;j<=n;j++)  //交换a的行
    	{
    		double x1;
    		x1=a[k][j];
    		a[k][j]=a[n1][j];
    		a[n1][j]=x1;
    	}
    	double b1;   //交换b的行
		b1=b[k];
		b[k]=b[n1];
		b[n1]=b1;
		//System.out.println("交换后：");
		//PrintA();
    	}
    }
    public void Determinant(){//求行列式
    	double DM=1.0;
    	for(int i=1;i<=n;i++)
    	{
    		double a2=a[i][i];
    	    DM=DM*a2;
    	}
    	double n3=(double)n2;
    	DM=DM*Math.pow(-1.0, n3);
    	System.out.println("该方程组的系数行列式：det A = "+DM);
    }
    public void PrintA(){//输出增广矩阵
    	System.out.println("增广矩阵为：");
    	for(int i=1;i<=n;i++)
    	{
    		for(int j=1;j<=n;j++)
    			System.out.print(a[i][j]+"    ");
    		System.out.print(b[i]+"    ");
    		System.out.print("\n");
    	}
    }
    public void Print(){//输出方程的根
    	System.out.println("方程组的根为：");
    	for(int i=1;i<=n;i++)
    		System.out.println("x"+i+" = "+x[i]);
    }
	public static void main(String[] args) {
		Scanner as=new Scanner(System.in);
        System.out.println("输入方程组的元数：");
        int k=as.nextInt();
        GaussianElimination ge=new GaussianElimination(k);
        /*
        ge.n=as.nextInt();
        ge.a=new double[ge.n+1][ge.n+1];
        ge.b=new double[ge.n+1];
        ge.x=new double[ge.n+1];
        */
        System.out.println("输入方程组的系数矩阵a：");
        for(int i=1;i<=ge.n;i++)
        	for(int j=1;j<=ge.n;j++)
        		ge.a[i][j]=as.nextDouble();
        System.out.println("输入方程组矩阵b：");
        for(int i=1;i<=ge.n;i++)
        	ge.b[i]=as.nextDouble();
        ge.Elimination();
        ge.Back();
        ge.Print();
        ge.Determinant();
	}
}

