/***********************************/
/* OtsuThresholdSelection          */
/*---------------------------------*/
/* The Otsu threshold selection m- */
/* ethod for gray level histogram  */
/* of image.                       */
/*---------------------------------*/
/* Date:090215                     */
/*---------------------------------*/
/* Program by Fu,Yu-Hsiang in Aizu */
/***********************************/

//Import
import java.lang.*;
import java.io.*;
import java.util.*;
import java.text.*;

//import AWT
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

//import Swing
import javax.imageio.*;
import javax.swing.*;

public class OtsuThresholdSelection{
    //Otsu parameters
    private int L;//Gray levels
    private int M;//Number of class
    private int N;//Total number of pixel
    private int T;//Number of threshold
    private int[] t;//Thresholds
    private int[] n;//Number of each level of pixel
    private double[] p;//Probability of each level of pixel
    private double[] w;//Weight of each class
    private double uT;//Total mean
    private double[] u;//Mean of each class
    private double oT;//Total variance
    private double oB;//Inter-class variance
    private double oW;//Intra-class variance
    private double[] o;//Variance of each class
    private double lamda,kila,nu;

    //Optimal set
    private int[] optimal_t;
    private double optimal_oB,optimal_uT,optimal_oT,optimal_nu;
    private double[] optimal_w,optimal_u,histogram_oB;

    //Graph
    private int counter;
    private int[] visited;
    private int[][] g;

    //Images
    private String filename,pathin;
    private BufferedImage bimage_ori,bimage_gray,bimage_otsu;
    private ImageIcon ii_ori,ii_gray,ii_otsu;
    private int image_height,image_width;
    private int[] bimageArray_ori,bimageArray_gray,bimageArray_otsu;
    private int[] histogram_gray;

    //Time
    private float time;

	//GUI Component
	private JFrame jframe_control,jframe_image;
	private Container ccontrol,cimage;
	private JTextArea jtarea;
	private JScrollPane jspane;
	private JPanel jpcontrol,jpparameter;
	private JPanel jpimage_ori,jpimage_gray,jpimage_otsu;
    private JLabel jlimage_ori,jlimage_gray,jlimage_otsu;
    private JLabel jlimage_ori_info,jlimage_gray_info,jlimage_otsu_info;
	private JButton jbload,jbrun,jbsave,jbexit;
	private JLabel jlimage,jllevel,jlclass;
	private JTextField jtfimage,jtflevel,jtfclass;
	private JFileChooser jfc;

    public OtsuThresholdSelection(){
    	//GUI interface
	    GUIInterface();
    }

    //GUIInterface
    private void GUIInterface(){
        //Control frame
        jframe_control = new JFrame("Aizu - Advanced Image Processing and Algorithm - Final project");
        jframe_control.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe_control.setSize(400,275);
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    jframe_control.setLocation(((screenSize.width-jframe_control.getSize().width)/2),
                                   ((screenSize.height-jframe_control.getSize().height)/2));

        //Container
        ccontrol = new Container();
		ccontrol.setLayout(new GridLayout(3,1));
		jframe_control.add(ccontrol);

        //Display Area
	    jtarea = new JTextArea("Aizu - Advanced Image Processing and Algorithm - Final project");
	    jtarea.append("\n--");
	    jtarea.append("\nOtsu threshold selection.");
	    jtarea.append("\nProgram by Fu, Yu-Hsiang (m5128110), 02/15/2009 in Aizu.");
	    jtarea.append("\n--");
	    jtarea.setEditable(false);
	    jtarea.setDisabledTextColor(Color.BLACK);
		jspane = new JScrollPane(jtarea);
	    ccontrol.add(jspane);

        //Parameter panel
        jpparameter = new JPanel();
        jpparameter.setBorder(BorderFactory.createTitledBorder("Parameter"));
        ccontrol.add(jpparameter);

        Box bimage = Box.createHorizontalBox();
        jlimage = new JLabel("Image:");
        jtfimage = new JTextField();
        jtfimage.setColumns(8);
        jtfimage.setEditable(false);
        bimage.add(jlimage);
        bimage.add(jtfimage);
        jpparameter.add(bimage);

        Box blevel = Box.createHorizontalBox();
        jllevel = new JLabel("Levels:");
        jtflevel = new JTextField();
        jtflevel.setColumns(5);
        jtflevel.setText("256");
        blevel.add(jllevel);
        blevel.add(jtflevel);
        jpparameter.add(blevel);

        Box bclass = Box.createHorizontalBox();
        jlclass = new JLabel("Classes:");
        jtfclass = new JTextField();
        jtfclass.setColumns(5);
        jtfclass.setText("2");
        bclass.add(jlclass);
        bclass.add(jtfclass);
        jpparameter.add(bclass);

        //Control panel
        jpcontrol = new JPanel();
        jpcontrol.setBorder(BorderFactory.createTitledBorder("Control"));
        ccontrol.add(jpcontrol);

        //Button
        jbload = new JButton("Load");
	    jbload.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
                    jfc = new JFileChooser();
                    int retval = jfc.showOpenDialog(null);
                    if (retval == JFileChooser.APPROVE_OPTION){
                        File f = jfc.getSelectedFile();
                        filename = f.getName();
                        pathin = f.getPath();
                        jtfimage.setText(filename);
                        jtarea.append("\nfile:"+filename+".");
                        jtarea.append("\npath:"+pathin+".");
                        jtarea.append("\n--");
                        jbrun.setEnabled(true);
                    }
				}
			}
		);
        jbrun = new JButton("Run");
        jbrun.setEnabled(false);
	    jbrun.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
    				jtarea.append("\nProcess.");

                    //Initialization
                    Initialization();

                    //LoadImage
                    LoadImage();

                    //OtsuProcesses
                    OtsuProcesses();

                    //Show image
                    ShowImage();

                    jbrun.setEnabled(false);
                    jbsave.setEnabled(true);
				}
			}
		);
        jbsave = new JButton("Save");
	    jbsave.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
    				jtarea.append("\nSave images.");

    				//Outout
                    Output();

                    jbrun.setEnabled(true);
                    jbsave.setEnabled(false);
                    jframe_image.setVisible(false);
				}
			}
		);
        jbsave.setEnabled(false);
        jbexit = new JButton("Exit");
	    jbexit.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
    				jtarea.append("\nExit!!");
					JOptionPane.showMessageDialog(null,"Have a nice day! Bye~ : )");
					System.exit(0);
				}
			}
		);
		jpcontrol.add(jbload);
        jpcontrol.add(jbrun);
        jpcontrol.add(jbsave);
        jpcontrol.add(jbexit);

        //Show frame
        jframe_control.setVisible(true);

        ////////////////////////

        //Show image
        jframe_image = new JFrame("Aizu - Advanced Image Processing and Algorithm - Final project");
        jframe_image.setSize(900,330);
        jframe_image.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	    jframe_image.setLocation(((screenSize.width-jframe_image.getSize().width)/2),
                                   ((screenSize.height-jframe_image.getSize().height)/2));

        //Container
        cimage = new Container();
        cimage.setLayout(new GridLayout(1,3));
		jframe_image.add(cimage);

		//Original image
		jpimage_ori = new JPanel();
        jpimage_ori.setBorder(BorderFactory.createTitledBorder("Original"));
        jpimage_ori.setLayout(new BorderLayout());
        ii_ori = new ImageIcon();
        jlimage_ori = new JLabel(ii_ori);
        jpimage_ori.add(jlimage_ori,BorderLayout.CENTER);
        jlimage_ori_info = new JLabel("Original image");
        jlimage_ori_info.setHorizontalAlignment(JLabel.CENTER);
        jpimage_ori.add(jlimage_ori_info,BorderLayout.SOUTH);
        cimage.add(jpimage_ori);

        //Gray image
		jpimage_gray = new JPanel();
        jpimage_gray.setBorder(BorderFactory.createTitledBorder("Gray"));
        jpimage_gray.setLayout(new BorderLayout());
        ii_gray = new ImageIcon();
        jlimage_gray = new JLabel(ii_gray);
        jpimage_gray.add(jlimage_gray,BorderLayout.CENTER);
        jlimage_gray_info = new JLabel("Gray image");
        jlimage_gray_info.setHorizontalAlignment(JLabel.CENTER);
        jpimage_gray.add(jlimage_gray_info,BorderLayout.SOUTH);
        cimage.add(jpimage_gray);

        //Otsu image
		jpimage_otsu = new JPanel();
        jpimage_otsu.setBorder(BorderFactory.createTitledBorder("Otsu"));
        jpimage_otsu.setLayout(new BorderLayout());
        ii_otsu = new ImageIcon();
        jlimage_otsu = new JLabel(ii_otsu);
        jpimage_otsu.add(jlimage_otsu,BorderLayout.CENTER);
        jlimage_otsu_info = new JLabel();
        jlimage_otsu_info.setHorizontalAlignment(JLabel.CENTER);
        jpimage_otsu.add(jlimage_otsu_info,BorderLayout.SOUTH);
        cimage.add(jpimage_otsu);
    }

    //Initialization
    private void Initialization(){
        L=Integer.parseInt(jtflevel.getText());
        M=Integer.parseInt(jtfclass.getText());
        T=M-1;
        N=0;
        t = new int[T];
        n = new int[L];
        p = new double[L];
        w = new double[M];
        u = new double[M];
        o = new double[M];

        uT=0;
        oB=0;
        oW=0;
        lamda=0;
        kila=0;
        nu=0;

        optimal_oB=0;
        optimal_uT=0;
        optimal_oT=0;
        optimal_nu=0;
        optimal_t = new int[T];
        optimal_w = new double[M];
        optimal_u = new double[M];
        histogram_oB = new double[L];
    }

    private void Reset(){
        if(T==1){
            t = new int[T];
        }
        w = new double[M];
        u = new double[M];
        o = new double[M];
        uT=0;
        oT=0;
        oB=0;
        oW=0;
    }

    //LoadImage
    private void LoadImage(){
        try{
            //Read image
            bimage_ori = ImageIO.read(new File(pathin));

            //Image height and width
            image_height = bimage_ori.getHeight();
            image_width = bimage_ori.getWidth();

            //Image array
            bimageArray_ori = bimage_ori.getRGB(0,0,image_width,image_height,null,0,image_width);
            bimageArray_gray = bimage_ori.getRGB(0,0,image_width,image_height,null,0,image_width);
            bimageArray_otsu = bimage_ori.getRGB(0,0,image_width,image_height,null,0,image_width);

            //Transform to gray
            doGray(bimageArray_gray);
        }
		catch(FileNotFoundException e){
			jtarea.append("\nOtsuThresholdSelection-ReadImage:檔案找不到!!");
		}
		catch(IOException e){
			jtarea.append("\nOtsuThresholdSelection-ReadImage:I/O錯誤!!");
		}
        catch(Exception e){
            jtarea.append("\nOtsuThresholdSelection-ReadImage:例外錯誤!!");
        }
    }

    private void doGray(int[] bimageArray_gray){
        //Gray image histogram
        histogram_gray = new int[256];

        //Transform to gray
        for(int a=0;a<bimageArray_gray.length;a++){
                int rgb = bimageArray_gray[a];
                int r=(rgb&0xff0000)>>16;
                int g=(rgb&0xff00)>>8;
                int b=(rgb&0xff);
                int gray=(r+g+b)/3;

                rgb=(0xff000000|(gray<<16)|(gray<<8)|gray);
                bimageArray_gray[a]=rgb;
                histogram_gray[gray]++;

                //N of Otsu method
                n[gray%L]++;
                N++;
        }

        //Creage gray image
        bimage_gray = CreageBufferedImage(bimageArray_gray,image_height,image_width);
    }

    private BufferedImage CreageBufferedImage(int[] bimageArray_gray,int image_height,int image_width){
        DataBuffer db = new DataBufferInt(bimageArray_gray,image_height*image_width);
        WritableRaster raster =Raster.createPackedRaster(db,image_width,image_height,image_width,new int[]{0xff0000,0xff00,0xff},null);
        ColorModel cm = new DirectColorModel(24,0xff0000,0xff00,0xff);

        return new BufferedImage(cm,raster,false,null);
    }

    //OtsuProcesses
    private void OtsuProcesses(){
        //P
        for(int a=0;a<p.length;a++){
            p[a]=(double)n[a]/(double)N;
        }

        long start = System.currentTimeMillis();//Start time

        //Otsu threaholding
        if(T==1){
            SingleThreshold();//Two classes
        }
        else{
            MultiThresholds();//Multi classes
        }

        float end = (System.currentTimeMillis()-start)/1000F;//End time
        time=end;

        //Define gray colors
        int[] grayColors = new int[M];

        if(T==1){
            grayColors[0]=0;
            grayColors[1]=255;
        }
        else{
            grayColors[0]=0;
            grayColors[M-1]=255;

            int addColor=255/T;

            for(int a=1;a<(M-1);a++){
                grayColors[a]=grayColors[a-1]+addColor;
            }
        }

        //Decide gray color
        for(int a=0;a<bimageArray_otsu.length;a++){
                int rgb = bimageArray_otsu[a];
                int r=(rgb&0xff0000)>>16;
                int g=(rgb&0xff00)>>8;
                int b=(rgb&0xff);
                int gray=(r+g+b)/3;

                gray=gray%L;

                for(int c=0;c<optimal_t.length;c++){
                    if(T==1){
                        if(gray<optimal_t[0]){
                            gray=grayColors[0];
                        }
                        else{
                            gray=grayColors[1];
                        }
                    }
                    else{
                        if(c==0 && (0<=gray && gray<optimal_t[c])){
                            gray=grayColors[c];
                        }
                        else if(c!=0 && (optimal_t[c-1]<=gray && gray<optimal_t[c])){
                            gray=grayColors[c];
                        }
                        else if(c==(optimal_t.length-1) && optimal_t[c-1]<=gray){
                            gray=grayColors[optimal_t.length];
                        }
                    }
                }

                rgb=(0xff000000|(gray<<16)|(gray<<8)|gray);
                bimageArray_otsu[a]=rgb;
        }

        //Create otsu image
        bimage_otsu = CreageBufferedImage(bimageArray_otsu,image_height,image_width);
    }

    private void SingleThreshold(){
        for(int k=0;k<L;k++){
            t[0]=k;

            for(int a=0;a<t[0];a++){
                w[0]+=p[a];
                u[0]+=a*p[a];
            }
            for(int a=t[0];a<L;a++){
                w[1]+=p[a];
                u[1]+=a*p[a];
            }

            for(int a=0;a<u.length;a++){
                u[a]=(u[a]/w[a]);
            }
            for(int a=0;a<u.length;a++){
                uT+=(w[a]*u[a]);
            }

            for(int a=0;a<t[0];a++){
                o[0]+=Math.pow((a-u[0]),2)*p[a]/w[0];
            }
            for(int a=t[0];a<L;a++){
                o[1]+=Math.pow((a-u[1]),2)*p[a]/w[1];
            }

            for(int a=0;a<o.length;a++){
                oW+=(w[a]*o[a]);
            }
            for(int a=0;a<u.length;a++){
                oB+=w[a]*Math.pow((u[a]-uT),2);

                //Check NaN, Infinite
                if(Double.isNaN(oB) || Double.isInfinite(oB)){
                    oB=0;
                }
            }

            oT=oW+oB;

            lamda=oB/oW;
            kila=oT/oW;
            nu=oB/oT;

            histogram_oB[t[0]]=oB;

            if(oB>optimal_oB){
                optimal_t[0]=t[0];
                for(int a=0;a<optimal_w.length;a++){
                    optimal_w[a]=w[a];
                }
                for(int a=0;a<optimal_u.length;a++){
                    optimal_u[a]=u[a];
                }
                optimal_uT=uT;
                optimal_oB=oB;
                optimal_oT=oT;
                optimal_nu=nu;

            }

            Reset();
        }

        //jtarea.append("\n"+optimal_t[0]+","+optimal_oB);
    }

    private void MultiThresholds(){
        //Trhesholds
        t = new int[T];

        //Creat graph
        g = new int[T][L];

        for(int a=0;a<T;a++){
            for(int b=a;b<(L-(T-1))+a;b++){
                g[a][b]=1;
            }
        }

        for(int a=0;a<(L-(T-1));a++){
            visited = new int[L];
            t[counter]=a;

            dfs(a);

            counter--;
            t[counter]=0;
        }

        //for(int a=0;a<optimal_t.length;a++){
        //    jtarea.append("\n"+optimal_t[a]+",");
        //}
        //jtarea.append("\n"+optimal_oB);
    }

    private void dfs(int v){
        visited[v]=1;
        counter++;

        if(counter<T){
            for(int a=0;a<L;a++){
                if(a>v && visited[a]==0){
                    t[counter]=a;
                    if(counter==(T-1)){
                        doMultiThresholding(t);//Each combination
                    }

                    dfs(a);

                    visited[a]=0;
                    counter--;
                    t[counter]=0;
                }
            }
        }
    }

    private void doMultiThresholding(int[] t){
        for(int a=0;a<=t.length;a++){
            if(a==0){
                for(int b=0;b<t[a];b++){
                    w[a]+=p[b];
                    u[a]+=b*p[b];
                }
            }
            else if(a==t.length){
                for(int b=t[a-1];b<L;b++){
                    w[a]+=p[b];
                    u[a]+=b*p[b];
                }
            }
            else{
                for(int b=t[a-1];b<t[a];b++){
                    w[a]+=p[b];
                    u[a]+=b*p[b];
                }
            }
        }

        for(int a=0;a<u.length;a++){
            u[a]=(u[a]/w[a]);
        }
        for(int a=0;a<u.length;a++){
            uT+=(w[a]*u[a]);
        }

        for(int a=0;a<=t.length;a++){
            if(a==0){
                for(int b=0;b<t[a];b++){
                    o[a]+=Math.pow((b-u[a]),2)*p[b]/w[a];
                }
            }
            else if(a==t.length){
                for(int b=t[a-1];b<L;b++){
                    o[a]+=Math.pow((b-u[a]),2)*p[b]/w[a];
                }
            }
            else{
                for(int b=t[a-1];b<t[a];b++){
                    o[a]+=Math.pow((b-u[a]),2)*p[b]/w[a];
                }
            }
        }

        for(int a=0;a<o.length;a++){
            oW+=(w[a]*o[a]);
        }
        for(int a=0;a<u.length;a++){
            oB+=w[a]*Math.pow((u[a]-uT),2);

            //Check NaN, Infinite
            if(Double.isNaN(oB) || Double.isInfinite(oB)){
                oB=0;
            }
        }
        oT=oW+oB;

        lamda=oB/oW;
        kila=oT/oW;
        nu=oB/oT;

        if(oB>optimal_oB){
            for(int a=0;a<optimal_t.length;a++){
                optimal_t[a]=t[a];
            }
            for(int a=0;a<optimal_w.length;a++){
                optimal_w[a]=w[a];
            }
            for(int a=0;a<optimal_u.length;a++){
                optimal_u[a]=u[a];
            }
            optimal_uT=uT;
            optimal_oB=oB;
            optimal_oT=oT;
            optimal_nu=nu;
        }

        Reset();
    }

    private void ShowImage(){
        //Set display image
        ii_ori.setImage(bimage_ori);
        ii_gray.setImage(bimage_gray);
        ii_otsu.setImage(bimage_otsu);

        //Information of Otsu image
        String otsuimage_info="<html><body>";

        otsuimage_info+="L:"+L+" ";
        otsuimage_info+="M:"+M;
        otsuimage_info+="<br>";
        for(int a=0;a<optimal_t.length;a++){
            otsuimage_info+="t"+a+":"+optimal_t[a]+" ";
        }
        otsuimage_info+="oB:"+Math.round(optimal_oB*100.0+0.5)/100.0;
        otsuimage_info+="</body></html>";

        jlimage_otsu_info.setText(otsuimage_info);

        //Show frame
        //jframe_image.pack();
        jframe_image.setVisible(true);
    }

    //Output
    private void Output(){
        try{
            if(filename.indexOf(".")!=-1){
                filename=filename.substring(0,filename.indexOf("."));
            }

            //Output Otsu information
            BufferedWriter bw = new BufferedWriter(new FileWriter("output\\"+filename+"_otsu_"+M+"_info.txt"));

            bw.write("Info");
            bw.newLine();
            bw.write("--");
            bw.newLine();
            for(int a=0;a<optimal_t.length;a++){
                bw.write("t"+a+": "+optimal_t[a]);
                bw.newLine();
            }
            bw.newLine();
            for(int a=0;a<optimal_w.length;a++){
                bw.write("w"+a+": "+optimal_w[a]);
                bw.newLine();
            }
            bw.newLine();
            for(int a=0;a<optimal_u.length;a++){
                bw.write("u"+a+": "+optimal_u[a]);
                bw.newLine();
            }
            bw.newLine();
            bw.write("uT: "+optimal_uT);
            bw.newLine();
            bw.write("oB: "+optimal_oB);
            bw.newLine();
            bw.write("oT: "+optimal_oT);
            bw.newLine();
            bw.write("nu: "+optimal_nu);
            bw.newLine();
            bw.newLine();
            bw.write("time: "+time);
            bw.newLine();
            bw.close();

            //Output histogram
            bw = new BufferedWriter(new FileWriter("output\\"+filename+"_gray_his.txt"));

            for(int a=0;a<histogram_gray.length;a++){
                bw.write(String.valueOf(histogram_gray[a]));
                bw.newLine();
            }
            bw.close();

            if(T==1){
                bw = new BufferedWriter(new FileWriter("output\\"+filename+"_otsu_"+M+"_oB_his.txt"));

                for(int a=0;a<histogram_oB.length;a++){
                    bw.write(String.valueOf(histogram_oB[a]));
                    bw.newLine();
                }
                bw.close();
            }

            //Output image
            ImageIO.write(bimage_gray,"jpg",new File("output\\"+filename+"_gray.jpg"));
            ImageIO.write(bimage_otsu,"jpg",new File("output\\"+filename+"_otsu_"+M+".jpg"));
        }
		catch(FileNotFoundException e){
			jtarea.append("\nOtsuThresholdSelection-Output:檔案找不到!!");
		}
		catch(IOException e){
			jtarea.append("\nOtsuThresholdSelection-Output:I/O錯誤!!");
		}
        catch(Exception e){
            jtarea.append("\nOtsuThresholdSelection-Output:例外錯誤!!");
        }
    }

    public static void main(String args[]){
        OtsuThresholdSelection Ots = new OtsuThresholdSelection();
    }
}