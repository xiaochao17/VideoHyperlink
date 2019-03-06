import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.lang.NullPointerException;

public class VideoUI extends Frame implements ActionListener {

    private int timer = 0;
    private ArrayList<BufferedImage> images; 
    private BufferedImage img;
    private PlaySound playSound;
    private long frameRate = 31;
    
    private JLabel imageLabel;

    private Button playButton;
    private Button pauseButton;
    private Button stopButton;
    private Button importButton;

    private int playStatus = 3; //1 for play, 2 for pause, 3 for stop
    private String fileName;

    private Thread playingThread;
    private Thread audioThread;
    private Thread cacheThread;

    private int currentFrameNum = 0;
    private int totalFrameNum = 9000;

    static final int WIDTH = 352;
    static final int HEIGHT = 288;

    private File selectedFile;
    private String jsonFile;

    private Queue<BufferedImage> cacheQueue;
    private String[] filelist;
    private int framenumber;
    private Boolean importPressed = false;
    
	public VideoUI() {
		

		BufferedImage img;
		
	    addWindowListener(new WindowAdapter(){
		  public void windowClosing(WindowEvent we){
		    System.exit(0);
		  }
		});

	    //Video List Panel
	    Panel listPanel = new Panel();
	    listPanel.setLayout(new GridLayout(2, 1));

		img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	    
	    Panel imagePanel = new Panel();
	    imageLabel = new JLabel(new ImageIcon(img));
	    imageLabel.addMouseListener(new MouseAdapter() {
	      public void mouseClicked(MouseEvent e) {
	        if (e.getButton() == MouseEvent.BUTTON1) {
	        	int xPosition = e.getX();
	        	int yPosition = e.getY();
	        	int clickFrame = currentFrameNum;
	        	try{
	        		checkJson(clickFrame,xPosition,yPosition);
	        	} catch(IOException e1){
	        		e1.printStackTrace();
	        	}
	        } 

	        // System.out.println("Number of click: " + e.getClickCount());
	        // System.out.println("Click position (X, Y):  " + e.getX() + ", " + e.getY());
	      }
	    });
	    imagePanel.add(imageLabel);
	    listPanel.add(imagePanel);
	    
	    //Control Panel
	    Panel controlPanel = new Panel();
	    importButton = new Button("Import Video");
	    importButton.addActionListener(this);
	    controlPanel.add(importButton);

	    playButton = new Button("PLAY");
	    playButton.addActionListener(this);
	    controlPanel.add(playButton);
	    pauseButton = new Button("PAUSE");
	    pauseButton.addActionListener(this);
	    controlPanel.add(pauseButton);
	    stopButton = new Button("STOP");
	    stopButton.addActionListener(this);
	    controlPanel.add(stopButton);
	    
	    listPanel.add(controlPanel);
	    add(listPanel, BorderLayout.SOUTH);
	    

	}

	public void checkJson(int whichframe, int xPos, int yPos) throws IOException{
		JSONParser parser=new JSONParser();
		File testJson = new File(jsonFile);
		if (!testJson.isFile()){
			return;
		}
		try{
				JSONObject obj1 = (JSONObject)parser.parse(new FileReader(jsonFile));
				Map framemap = new HashMap();
			   for(Iterator iterator = obj1.keySet().iterator(); iterator.hasNext();) 
			   {
			    	
			        String key = (String) iterator.next();
			        Object obj2 = obj1.get(key);
				    JSONArray trackingobjects = (JSONArray)obj2;
				    ArrayList<ArrayList<String>> trackingframes = new ArrayList<ArrayList<String>>();
				    for (int i = 0; i < trackingobjects.size();i++)
				    {
				        ArrayList<String> trackingframe = new ArrayList<String>();
					     JSONArray trackingobject = (JSONArray)trackingobjects.get(i);
					     String position1 =  (String) trackingobject.get(0);
					     String position2 =  (String) trackingobject.get(1);
					     String position3 =  (String) trackingobject.get(2);
					     String position4 =  (String) trackingobject.get(3);
					     String targetframe = (String) trackingobject.get(4);
					     trackingframe.add(position1);
					     trackingframe.add(position2);
					     trackingframe.add(position3);
					     trackingframe.add(position4);
					     trackingframe.add(targetframe);
					     trackingframes.add(trackingframe);
				    }
			    
				    framemap.put(key,trackingframes);
				}

				if (framemap.containsKey(Integer.toString(whichframe))){
					
					ArrayList valuelist =(ArrayList)framemap.get(Integer.toString(whichframe));
					for (int i=0; i<valuelist.size();i++){
						String firstvalue = (String) (((ArrayList)valuelist.get(i)).get(0));
						String secondvalue = (String) (((ArrayList)valuelist.get(i)).get(1));
						String thirdvalue = (String) (((ArrayList)valuelist.get(i)).get(2));
						String forthvalue = (String) (((ArrayList)valuelist.get(i)).get(3));
						String secondVideoName = (String) (((ArrayList)valuelist.get(i)).get(4));

						int xCor = Integer.parseInt(firstvalue);
						int yCor = Integer.parseInt(secondvalue);
						int wd = Integer.parseInt(thirdvalue);
						int ht = Integer.parseInt(forthvalue);

						if(xPos>xCor && xPos<xCor+wd && yPos>yCor && yPos<yCor+ht){
							if(playingThread != null) {
								playingThread.interrupt();
								audioThread.interrupt();

							    // cacheThread.interrupt();

								playSound.stop();
								playingThread = null;
								audioThread = null;

								// cacheThread = null;

								// cacheQueue.clear();
								
								loadNextVideo(secondVideoName);
								return;
							} 
						}
					}

				}





		} catch(ParseException pe){
            pe.printStackTrace();
			
        }		

	}

	
	
	public void showUI() {
	    pack();
	    setVisible(true);
	}
	
	private void playVideo() {
		playingThread = new Thread() {
            public void run() {
	            while(currentFrameNum<9000) {
	            	long starttime = System.currentTimeMillis();

	            	try{
	          			imageLabel.setIcon(new ImageIcon(cacheQueue.poll()));
	          			
	          		} catch(NullPointerException e3){
	          			e3.printStackTrace();
	          		}
	          		long endtime = System.currentTimeMillis();
	          		long duration = endtime-starttime;
	          		if ((31-duration)>0){
	          			frameRate = 31-duration;
	          		}
	          		else{
	          			frameRate = 31;
	          		}
	          	    try {
					    
						
							sleep(frameRate);
						
						// System.out.println(System.currentTimeMillis());
	                  	
	                  	currentFrameNum++;
	          	    } catch (InterruptedException e) {
	          	    	if(playStatus == 3) {
	          	    		framenumber = 0;
	          	    		currentFrameNum = 0;
	          	    	} else {
	          	    		framenumber = currentFrameNum;
	          	    	}
	          	    	imageLabel.setIcon(new ImageIcon(cacheQueue.element()));
	          	    	
	          	    	// cacheQueue.clear();
	                   	currentThread().interrupt();
	                  	break;
	                }
	          	}
	          	if(playStatus < 2) {
	          		// System.out.println(currentFrameNum);
	          		// System.out.println("to the end, ready to stop");
	          		playStatus = 3;
		            stopVideo();


	          	}
	        }
	    };
	    audioThread = new Thread() {
            public void run() {
                try {
        	        playSound.play();
        	    } catch (PlayWaveException e) {
        	        e.printStackTrace();
        	        return;
        	    }
	        }
	    };
	    audioThread.start();
	    playingThread.start();
	}
		
	private void pauseVideo() throws InterruptedException {
		if(playingThread != null) {
			playingThread.interrupt();
			audioThread.interrupt();
			playSound.pause();
			playingThread = null;
			audioThread = null;
		}
	}
		
	private void stopVideo() {
		if(playingThread != null) {
			playingThread.interrupt();
			playingThread = null;
		}
		if (audioThread != null){
			audioThread.interrupt();
			playSound.stop();
			audioThread = null;
		}
			currentFrameNum = 0;
			cacheQueue.clear();
			framenumber = 0;
	
	}
	

	private void read_rgb_image(){
		Thread cacheThread = new Thread() {
            public void run() {
            	while (framenumber<9002){

            		if (framenumber<9000){

						if(cacheQueue.size()<400){

						    try{
		    					// System.out.println(filelist[framenumber]);

						    	FileInputStream colorimage = new FileInputStream(selectedFile.getAbsolutePath()+"/"+filelist[framenumber]);
						    	byte[] r = new byte[WIDTH*HEIGHT];
						        byte[] g = new byte[WIDTH*HEIGHT];
						        byte[] b = new byte[WIDTH*HEIGHT];
						        int[][] pico = new int[WIDTH][HEIGHT*3];
						        colorimage.read(r);
						        colorimage.read(g);
						        colorimage.read(b);
						        int count = 0;
						        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
						        for(int y = 0; y < HEIGHT*3; y=y+3){
						          for(int x = 0; x < WIDTH; x++){
						             int red = r[count];
						             int green = g[count];
						             int blue = b[count];
						             int pix = 0xff000000 | ((red & 0xff) << 16) | ((green & 0xff) << 8) | (blue & 0xff);
						             pico[x][y]=r[count];
						             pico[x][y+1]=g[count];
						             pico[x][y+2]=b[count];
						            image.setRGB(x,y/3,pix);
						            count++;
						          }
						        }
								cacheQueue.add(image);
								framenumber++;
								colorimage.close();
							}catch (FileNotFoundException e1) {
						        // e1.printStackTrace();
						      } catch (IOException e2) {
						        e2.printStackTrace();
						        // System.out.println("IOException");
						      } 

		            	}
		            	else{
		            		try 
								{
								    Thread.sleep(200);
									// System.out.println("read rgb InterruptedException");

								} 
								catch(InterruptedException e)
								{
								    currentThread().interrupt();
								    break;
								}
		            	}

            		}
            		else{
            			framenumber = 0;
            		}

	            	
            	}

	        }
	    };
	    cacheQueue.clear();
	    cacheThread.start();  	
	}
	
	private void loadVideo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
		    selectedFile = fileChooser.getSelectedFile();
		    // System.out.println("Selected file: " + selectedFile.getAbsolutePath());
		    if(playingThread != null) {
			playingThread.interrupt();
			playingThread = null;
			}
			if (audioThread != null){
				audioThread.interrupt();
				playSound.stop();
				audioThread = null;
			}
		}
	    if(selectedFile == null){
	    	return;
	    }


	    filelist = new String[9000];
	    String audioFilename = selectedFile.getAbsolutePath()+'/'+ selectedFile.getName() + ".wav";
	    jsonFile = selectedFile.getAbsolutePath()+ ".json";

	    filelist= selectedFile.list(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	      if(name.endsWith(".rgb")){
	            return true;
	      } 
	      else {
	            return false;
	      }
	     }
	    });
	    Arrays.sort(filelist);
	    

		cacheQueue = new LinkedList<BufferedImage>();
	    framenumber = 0;
	    read_rgb_image();

	    
        playSound = new PlaySound(audioFilename, framenumber);
	    this.playStatus = 3;
       	currentFrameNum = 0;
	    // displayScreenShot();
	}

	private void loadNextVideo(String secondFileName){



		int nameIndex = secondFileName.lastIndexOf("/");
		String pathname = secondFileName.substring(0,nameIndex);
		String rgbName = secondFileName.substring(nameIndex+1);
		int numberIndex = rgbName.length()-8;
		int secondNameIndex = pathname.lastIndexOf("/");

		int selectedFrame = Integer.parseInt(rgbName.substring(numberIndex,numberIndex+4));


		filelist = new String[9001-selectedFrame];

		String audioFilename = pathname + pathname.substring(secondNameIndex)+".wav";

		selectedFile = new File(pathname);
		jsonFile = selectedFile.getAbsolutePath()+ ".json";


	    filelist= selectedFile.list(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
	      if(name.endsWith(".rgb")){
	            return true;
	      } 
	      else {
	            return false;
	      }
	     }
	    });
	    Arrays.sort(filelist);
	    
	    framenumber = selectedFrame-1;
	 

	    read_rgb_image();

        playSound = new PlaySound(audioFilename,framenumber);
       	currentFrameNum = selectedFrame-1;
       	this.playStatus = 1;
       	this.playVideo();

	}
	

	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.playButton && importPressed) {
			// System.out.println("play button clicked");
			if(this.playStatus > 1) {
				this.playStatus = 1;
				this.playVideo();
			}
		} else if(e.getSource() == this.pauseButton) {
			// System.out.println("pause button clicked");
			if(this.playStatus == 1) {
				this.playStatus = 2;
				try {
					this.pauseVideo();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		} else if(e.getSource() == this.stopButton) {
			// System.out.println("stop button clicked");
			if(this.playStatus < 3) {
				this.playStatus = 3;
				this.stopVideo();
			}
		} else if(e.getSource() == this.importButton){
			importPressed = true;
			System.out.println("Import Video");
			this.loadVideo();
		}
	}


	public static void main(String[] args) {
    	VideoUI ui = new VideoUI();
		ui.showUI();
	}

}
