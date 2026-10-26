/*
 *  Copyright 2009,2010 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JAsioHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JAsioHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JAsioHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.synthbot.jasiohost;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * The <code>ExampleHost</code> demonstrates how to use an <code>AsioDriver</code> in order to read 
 * and write audio from a loaded ASIO driver. A small GUI is presented, allowing the user to select
 * any of the available ASIO drivers on the system. The <i>Start</i> button loads the driver and 
 * plays a 440Hz tone. The <i>Stop</i> button stops this process and unloads the driver. The
 * <i>Control Panel</i> button opens the driver's control panel for any additional configuration.
 */
public class ExampleHost extends JFrame implements AsioDriverListener {
  
  private static final long serialVersionUID = 1L;
  
  private AsioDriver asioDriver;
  private Set<AsioChannel> activeChannels;
  private int sampleIndex;
  private int bufferSize;
  private double sampleRate;
  private float[] output;
  
  public ExampleHost() {
    super("JAsioHost Example");
    
    activeChannels = new HashSet<AsioChannel>();
    
    final JComboBox comboBox = new JComboBox(AsioDriver.getDriverNames().toArray());
    final JButton buttonStart = new JButton("Start");
    final JButton buttonStop = new JButton("Stop");
    final JButton buttonControlPanel = new JButton("Control Panel");
    
    final AsioDriverListener host = this;
    buttonStart.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (asioDriver == null) {
          asioDriver = AsioDriver.getDriver(comboBox.getSelectedItem().toString());
          asioDriver.addAsioDriverListener(host);
          activeChannels.add(asioDriver.getChannelOutput(0));
          activeChannels.add(asioDriver.getChannelOutput(1));
          sampleIndex = 0;
          bufferSize = asioDriver.getBufferPreferredSize();
          sampleRate = asioDriver.getSampleRate();
          output = new float[bufferSize];
          asioDriver.createBuffers(activeChannels);
          asioDriver.start();
        }
      }
    });
    
    buttonStop.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (asioDriver != null) {
          asioDriver.shutdownAndUnloadDriver();
          activeChannels.clear();
          asioDriver = null;
        }
      }
    });

    buttonControlPanel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if (asioDriver != null && 
            asioDriver.getCurrentState().ordinal() >= AsioDriverState.INITIALIZED.ordinal()) {
          asioDriver.openControlPanel();          
        }
      }
    });
    
    this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    this.add(comboBox);
    panel.add(buttonStart);
    panel.add(buttonStop);
    panel.add(buttonControlPanel);
    this.add(panel);
    
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent event) {
        if (asioDriver != null) {
          asioDriver.shutdownAndUnloadDriver();
        }
      }
    });
    
    this.setSize(240, 85);
    this.setResizable(false);
    this.setVisible(true);
  }
  
  public void bufferSwitch(long systemTime, long samplePosition, Set<AsioChannel> channels) {
    for (int i = 0; i < bufferSize; i++, sampleIndex++) {
      output[i] = (float) Math.sin(2 * Math.PI * sampleIndex * 440.0 / sampleRate);
    }
    for (AsioChannel channelInfo : channels) {
      channelInfo.write(output);
    }
  }
  
  public void bufferSizeChanged(int bufferSize) {
    System.out.println("bufferSizeChanged() callback received.");
  }

  public void latenciesChanged(int inputLatency, int outputLatency) {
    System.out.println("latenciesChanged() callback received.");
  }

  public void resetRequest() {
    /*
     * This thread will attempt to shut down the ASIO driver. However, it will
     * block on the AsioDriver object at least until the current method has returned.
     */
    new Thread() {
      @Override
      public void run() {
        System.out.println("resetRequest() callback received. Returning driver to INITIALIZED state.");
        asioDriver.returnToState(AsioDriverState.INITIALIZED);
      }
    }.start();
  }

  public void resyncRequest() {
    System.out.println("resyncRequest() callback received.");
  }

  public void sampleRateDidChange(double sampleRate) {
    System.out.println("sampleRateDidChange() callback received.");
  }
  
  public static void main(String[] args) {
    @SuppressWarnings("unused")
    ExampleHost host = new ExampleHost();
  }

}
