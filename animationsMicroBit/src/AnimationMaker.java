/**
 * Created by Nikola Yotov on 04.2016. Contact me at nikolaiotov96@gmail.com
 * 1st year university final project: Animation maker for the Micro:Bit computer
 * A desktop program that simulates the screen of a Micro:Bit device.
 * It allows the user to create, view, save and export animations and pictures in a special format that can be played on the Micro:Bit.
 * I wrote this during the dawn of my programming journey so don't judge it too hard. I know the code is far from the most elegant solution ever :)
 */


import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.text.*;
import java.util.ArrayList;
import java.io.*;
public class AnimationMaker implements ActionListener, ChangeListener
{
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem open, saveFrame, saveAs, help;
	private JFrame frame;
	private JPanel mainPanel, controlsPanel;
	private GridBagConstraints gc;
	private JButton[][] pixel;
	private JButton save, clear,startAnimation, nextFrame, playAnimation, resume, delete;
	private JSlider colorSlider, frameSlider;
	private ArrayList<int[][]> animations;
	private boolean startStop;
	private JFileChooser fc;
	private Timer timer;
	private int currentFrame = 0;
	private String helpMessage = "Click on pixels to flick them on or off. Use the vertical slider to adjust brightness."
			+ "\nTo record an animation click Start Recording.\nConfigure how you want your frame to look like and click Next Frame to save it to the current frame sequence"
			+ ".\nTo play an animation click Play. "
			+ "Please note: if you click Start Recording New Animation after playing an animation you will lose your\ncurrently loaded animation. To resume"
			+ " editing please use Resume instead.\nUse the horizontal slider to examine and delete specific frames from the currently loaded animation.\n"
			+ "Use the File Menu to save the current frame or animation sequence or to load one from a save.\n"
			+ "Click on Help to show this message again.";
	
	public AnimationMaker()
	{
		//Initializing the UI
		fc = new JFileChooser();
		menuBar = new JMenuBar();
		menu = new JMenu("File");
		open = new JMenuItem("Open animation");
		open.addActionListener(this);
		saveFrame = new JMenuItem("Save Current Frame As");
		saveFrame.addActionListener(this);
		saveAs = new JMenuItem("Save Current Animation As");
		saveAs.addActionListener(this);
		help = new JMenuItem("Help");
		help.addActionListener(this);
		menu.add(open);
		menu.add(saveFrame);
		menu.add(saveAs);
		menu.add(help);
		menuBar.add(menu);
		
		timer =  new Timer(600, this);
		timer.setInitialDelay(0);
		startStop = false;
		animations = new ArrayList<int[][]>();
		frame = new JFrame("Animation maker");
		mainPanel = new JPanel(new GridBagLayout());
		startAnimation = new JButton("Start Recording Animation");
		startAnimation.addActionListener(this);
		nextFrame = new JButton("Next Frame");
		nextFrame.addActionListener(this);
		delete = new JButton("Delete Current Frame");
		delete.addActionListener(this);
		playAnimation = new JButton("Play Animation");
		playAnimation.addActionListener(this);
		resume = new JButton("Resume Recording Animation");
		resume.addActionListener(this);
		resume.setEnabled(false);
		save = new JButton("Save Current Grid");
		save.addActionListener(this);
		clear = new JButton("Clear");
		clear.addActionListener(this);
		colorSlider = new JSlider(JSlider.VERTICAL, 0, 255, 255);
		colorSlider.setMajorTickSpacing(15);
		colorSlider.setPaintTicks(true);
		colorSlider.setPaintLabels(true);
		frameSlider = new JSlider(JSlider.HORIZONTAL, 1, 1, 1);
		frameSlider.addChangeListener(this);
		frameSlider.setPaintTicks(true);
		frameSlider.setPaintLabels(true);
		frameSlider.setMajorTickSpacing(1);
		controlsPanel = new JPanel(new GridBagLayout());
		gc = new GridBagConstraints();
		pixel = new JButton[5][5];
		gc.weightx = 1;
		gc.weighty = 1;
		gc.fill = GridBagConstraints.BOTH;
		//Careful placement of the "pixels" on the grid
		for (int i = 1; i <= 5; i++)
		{	
			gc.gridy = i;
			for (int j = 1; j <= 5; j++)
			{
				gc.gridx = j;
				pixel[i - 1][j - 1] = new JButton();
				pixel[i - 1][j - 1].setBackground(Color.WHITE);
				pixel[i - 1][j - 1].setFocusable(false);
				pixel[i - 1][j - 1].setFocusPainted(false);
				pixel[i - 1][j - 1].addActionListener(this);
				pixel[i - 1][j - 1].setRolloverEnabled(false);
				mainPanel.add(pixel[i - 1][j - 1], gc);
			}
		}
		//Manually adding all the control buttons
		gc.gridx = 6;
		gc.gridy = 1;
		gc.gridheight = 5;
		gc.weightx = 3;
		mainPanel.add(controlsPanel, gc);
		
		gc.weightx = 1;
		gc.gridheight = 1;
		gc.gridx = 1;
		controlsPanel.add(startAnimation, gc);		
		gc.gridy = 2;
		controlsPanel.add(nextFrame, gc);
		gc.gridy = 3;
		controlsPanel.add(playAnimation, gc);
		gc.gridy = 4;
		gc.gridwidth = 2;
		controlsPanel.add(frameSlider, gc);
		gc.gridx = 2;
		gc.gridwidth = 1;
		
		gc.gridy = 1;
		controlsPanel.add(resume, gc);
		gc.gridy = 2;
		controlsPanel.add(delete, gc);
		gc.gridy = 3;
		controlsPanel.add(clear, gc);
		

		gc.gridx = 3;
		gc.gridy = 1;		
		gc.gridheight = 4;
		controlsPanel.add(colorSlider, gc);
		
		frame.setJMenuBar(menuBar);
		frame.setSize(1200, 640);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setContentPane(mainPanel);
		frame.setVisible(true);
		JOptionPane.showMessageDialog(frame, helpMessage);
	}
	//Stops the timer that animates
	private void stopAnimation()
	{
		playAnimation.setText("Play Animation");
		timer.stop();
	}
	//Changes the current pixel configuration of the grid to the one passed as an argument
	private void changeFrame(int[][] frame)
	{
		for (int i = 0; i < 5; i++)
		{						
			for (int j = 0; j < 5; j++)
			{
				pixel[i][j].setBackground(new Color(frame[i][j], frame[i][j], frame[i][j]));
			}						
		}
	}
	//Clears the grid
	private void clearFrame()
	{
		for (int i = 0; i < 5; i++)
			for (int j = 0; j < 5; j++)
			{
				pixel[i][j].setBackground(Color.WHITE);
			}
	}

	//Really long action performed method that finds the source of the click event and acts accordingly
	public void actionPerformed(ActionEvent e)
	{
		Object clicked = e.getSource();
		if (clicked.equals(timer))
		{			
			if(!(animations.isEmpty()))
			{
				if (currentFrame < animations.size())
				{
					int[][] frame = animations.get(currentFrame);
					changeFrame(frame);
					currentFrame++;
				}
				else
				{
					currentFrame = 0;
				}				
			}
		}
		//handles file opening
		else if (clicked.equals(open))
		{
			stopAnimation();
			ArrayList<int[]> values = new ArrayList<int[]>();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".C source file", "currentFrame");
			fc.setFileFilter(filter);
			int returnVal = fc.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();
				BufferedReader br;
				try 
				{
					br = new BufferedReader(new FileReader(file));
					String line;
					while ( (line = br.readLine()) != null)
					{
						if (!(line.contains("const")))
						{		
							line = line.substring(0, line.length() - 3);
							if(line.contains("\\"))
								line = line.substring(0, line.length() - 1);							
							String delims = ",";
							String[] tokens = line.split(delims);
							int[] ints = new int[tokens.length];
							for (int i = 0; i < tokens.length; i++)
								ints[i] = Integer.parseInt(tokens[i]);
							values.add(ints);
						}						
					}
					br.close();
					int[] ints = values.get(0);
					int i = ints.length/5;
					int[][][] frames = new int[i][5][5];
					animations.clear();
					int l = 0;
					for (i = 0; i < 5; i++)
					{
						ints = values.get(i);
						for(int j = 0; j < ints.length; j++)
						{
							frames[l][i][j - l * 5] = 255 - ints[j];
							if(((j + 1) % 5) == 0)
								l++;
						}
						l = 0;
					}
					for(i = 0; i < ints.length/5; i++)
						animations.add(frames[i]);
					changeFrame(animations.get(0));
					frameSlider.setMaximum(animations.size());
				}catch (IOException e1) {e1.printStackTrace();}
			}
			
		}
		else if (clicked.equals(saveAs))
		{
			//Parsing the configured animation to a string readable by the Micro:Bit and then saving it as a .c file
			stopAnimation();
			if (!(animations.isEmpty()))
			{
				startStop = false;
				startAnimation.setText("Start Recording Animation");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(".C source file", "currentFrame");
				fc.setFileFilter(filter);
				int returnVal = fc.showSaveDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) 
				{
					String frame = new String();
					frame += "const char * const smiley =" + '"' + "\\" + "\r\n" ;
					for (int i = 0; i < 5; i++)
						for (int j = 0; j < animations.size(); j++)
						{	
							int[][] ints = animations.get(j);
							DecimalFormat myFormatter = new DecimalFormat("000");
							for (int k = 0; k < 5; k++)
							{
								frame += myFormatter.format(255 - ints[i][k]);
								if (i == 4 &&  j == animations.size() - 1 && k == 4)
									frame +="\\" + "n" + '"' + ";";
								else if (j == animations.size() - 1 && k == 4)
									frame +="\\" + "n" + "\\" + "\r\n";
								else if (!(j == animations.size() - 1 && k == 4))
									frame += ",";
							}
						}
					File file = fc.getSelectedFile();
					if(!fc.getSelectedFile().getAbsolutePath().endsWith(".currentFrame"))
					{
					    file = new File(fc.getSelectedFile() + ".currentFrame");
					}
					BufferedWriter bw;
		            try 
		            {
		                bw = new BufferedWriter(new FileWriter(file));
		                bw.write(frame);
		                bw.flush();
		            }               
		            catch (IOException e1)
		            {
		                e1.printStackTrace();
		            }
		            
		        }
			}
		}
		//Deletes a frame
		else if (clicked.equals(delete))
		{
			if(!(animations.isEmpty()))
			{
				animations.remove(frameSlider.getValue() - 1);
				frameSlider.setMaximum(animations.size());
			}			
		}
		//Clears the grid
		else if(clicked.equals(clear))
		{
			stopAnimation();
			clearFrame();		
		}
		//Parsing the configured animation to a string readable by the Micro:Bit and then saving it as a .c file
		else if (clicked.equals(saveFrame))
		{
			stopAnimation();
			FileNameExtensionFilter filter = new FileNameExtensionFilter(".C source file", "currentFrame");
			fc.setFileFilter(filter);
			int returnVal = fc.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				String emoji = new String();
				emoji += "const char * const smiley =" + '"' + "\\" + "\r\n" ;

				for (int i = 0; i < 5; i++)
					for (int j = 0; j < 5; j++)
					{	
						DecimalFormat myFormatter = new DecimalFormat("000");
						emoji += myFormatter.format(255 - pixel[i][j].getBackground().getBlue());
						if (!(i == 4 && j == 4))
						emoji += ",";
						if (j == 4 && i != 4)
							emoji +="\\" + "n" + "\\" + "\r\n";
						else if (j == 4 && i == 4)
							emoji +="\\" + "n" + '"' + ";";
					}
				File file = fc.getSelectedFile();
				if(!fc.getSelectedFile().getAbsolutePath().endsWith(".currentFrame"))
				{
				    file = new File(fc.getSelectedFile() + ".currentFrame");
				}
				BufferedWriter bw;
	            try
				{
	                bw = new BufferedWriter(new FileWriter(file));
	                bw.write(emoji);
	                bw.flush();
	            }               
	            catch (IOException e1)
	            {
	                e1.printStackTrace();
	            }
	        }
	  	}
	  	//Start recording a new animation sequence
		else if (clicked.equals(startAnimation))
		{
			if(startStop == false)
			{
				resume.setEnabled(true);
				stopAnimation();
				startStop = true;
				startAnimation.setText("Stop Recording Animation");
				if (!(animations.isEmpty()))
					animations.clear();
				frameSlider.setMaximum(1);
			}
			else
			{
				startAnimation.setText("Start Recording New Animation");
				startStop = false;
			}
		}
		//Resumes editing mode
		else if (clicked.equals(resume))
		{
			stopAnimation();
			startStop = true;
			startAnimation.setText("Stop Recording Animation");
			
		}
		//Adds the frame to the current sequence
		else if (clicked.equals(nextFrame))
		{
			if(startStop == true)
			{
				int[][] ints = new int[5][5];
				for (int i = 0; i < 5; i++)
				{
					for (int j = 0; j < 5; j++)
					{
						ints[i][j] = pixel[i][j].getBackground().getBlue();
					}
					
				}
				animations.add(ints);
				frameSlider.setMaximum(animations.size());
				frameSlider.setValue(frameSlider.getMaximum());
			}
		}
		//Plays the currently loaded animation
		else if (clicked.equals(playAnimation))
		{
			if (!(timer.isRunning()) && !(animations.isEmpty()))
			{
				startStop = false;
				startAnimation.setText("Start Recording New Animation");
				playAnimation.setText("Stop Animation");
				clearFrame();
				timer.restart();
				
			}
			else
			{
				stopAnimation();
			}
		}
		//Displays the help text
		else if (clicked.equals(help))
			JOptionPane.showMessageDialog(frame, helpMessage);
		//Flicks a pixel on or off
		else
		{	int i = (255 - colorSlider.getValue());
			if (((JButton)(clicked)).getBackground().equals(Color.WHITE))
				((JButton)clicked).setBackground(new Color(i, i, i));
			else 
				((JButton)clicked).setBackground(Color.WHITE);
		}
	}

	//listener for the slider
	public void stateChanged(ChangeEvent e) 
	{
		int frame = frameSlider.getValue();
		if (!(animations.isEmpty()))
		{
			 int[][] ints = animations.get(frame - 1);
			 changeFrame(ints);	
		}
	}	
	
}