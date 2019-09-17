package com.example.hzl.udpjoystick;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.net.NetworkInterface;

import android.os.Handler;

import java.net.*;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends ActionBarActivity {
	public Handler handler = new Handler();

	//Global vars
	boolean Connected = false;
	List<String> ServerIP = new List<String>() {
		@Override
		public void add(int location, String object) {

		}

		@Override
		public boolean add(String object) {
			return false;
		}

		@Override
		public boolean addAll(int location, Collection<? extends String> collection) {
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends String> collection) {
			return false;
		}

		@Override
		public void clear() {

		}

		@Override
		public boolean contains(Object object) {
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> collection) {
			return false;
		}

		@Override
		public String get(int location) {
			return null;
		}

		@Override
		public int indexOf(Object object) {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		@NonNull
		@Override
		public Iterator<String> iterator() {
			return null;
		}

		@Override
		public int lastIndexOf(Object object) {
			return 0;
		}

		@NonNull
		@Override
		public ListIterator<String> listIterator() {
			return null;
		}

		@NonNull
		@Override
		public ListIterator<String> listIterator(int location) {
			return null;
		}

		@Override
		public String remove(int location) {
			return null;
		}

		@Override
		public boolean remove(Object object) {
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			return false;
		}

		@Override
		public String set(int location, String object) {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}

		@NonNull
		@Override
		public List<String> subList(int start, int end) {
			return null;
		}

		@NonNull
		@Override
		public Object[] toArray() {
			return new Object[0];
		}

		@NonNull
		@Override
		public <T> T[] toArray(T[] array) {
			return null;
		}
	};
	String DeterminedServerIP = "";
	LinkedBlockingQueue<String> DataToSend = new LinkedBlockingQueue<>();

	Thread receiveThread = new Thread(new Runnable() {
		@Override
		public void run() {
			try {
				final DatagramSocket receiveSocket = new DatagramSocket(9607);
				final TextView lbl_LocalIP = (TextView) MainActivity.this.findViewById(R.id.label_LocalIP);
				byte data[] = new byte[1024];

				//keep receiving
				DatagramPacket packet = new DatagramPacket(data, data.length);
				while (Connected == false) {
					receiveSocket.receive(packet);
					final String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
					String iPs[] = received.split(">");
					if (received.contains("<ip=")) {//deal with the received string

						for (int i = 0; i < iPs.length; i++) {
							iPs[i] = iPs[i].substring(4);
							ServerIP.add(iPs[i]);
						}
					}
					final String[] serverIPs = iPs;//used in UI

					//Update UI
					handler.post(new Runnable() {
						@Override
						public void run() {
							String str = "";
							if (serverIPs != null) {
								for (String s : serverIPs) {
									str += "\n" + s;
									final String final_ip = s;
									new AlertDialog.Builder(MainActivity.this)
											.setTitle("发现了一个服务器，要连接吗？")
											.setMessage(s)
											.setPositiveButton("嗯", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													//Determine the Server IP to use
													DeterminedServerIP = final_ip;
													Connected = true;
													SendDataThread.start();

													//send connect request
													try {

														MulticastSocket broadcastSocket = new MulticastSocket(7260);
														InetAddress broadcastAddress = InetAddress.getByName(DeterminedServerIP);
														String str = "<Connect>";
														byte data[] = str.getBytes();
														broadcastSocket.setBroadcast(true);
														DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, 7260);
														broadcastSocket.send(packet);
														broadcastSocket.close();
													}
													catch (Exception e) {
														new AlertDialog.Builder(MainActivity.this)
																.setTitle("哎呀。。。")
																.setMessage("连接失败。。。")
																.show();
													}
													return;
												}
											})
											.setNegativeButton("不要嘛", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {

												}
											})
											.show();
								}
								lbl_LocalIP.append(str);
							}
						}
					});
				}
			}
			catch (final Exception e) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						new AlertDialog.Builder(MainActivity.this)
								.setTitle("额")
								.setMessage(e.toString())
								.show();
					}
				});
			}
		}
	});
	Thread SendDataThread = new Thread(new Runnable() {
		@Override
		public void run() {
			handler.post(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(MainActivity.this)
							.setMessage("Hey, I am started!")
							.show();
				}
			});

			DatagramSocket send_Socket = null;
			InetAddress send_Address = null;
			try {
				send_Socket = new DatagramSocket(9696);
				send_Address = InetAddress.getByName(DeterminedServerIP);
			}
			catch (Exception e) {}
			while (true) {
				if (DataToSend.size()>0) {
					try {
						String sensorDataString = DataToSend.poll();
						byte sendDataByte[] = sensorDataString.getBytes();
						DatagramPacket send_Packet = new DatagramPacket(sendDataByte, sendDataByte.length, send_Address, 9696);
						send_Socket.send(send_Packet);
					}
					catch (final Exception e) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								new AlertDialog.Builder(MainActivity.this)
										.setMessage(e.toString())
										.show();
							}
						});
					}
				}
				else {try {this.wait(10);}catch (Exception e) {}}
			}
		}
	});

	public void Probe() {
		//clear container
		TextView lbl_LocalIP = (TextView) this.findViewById(R.id.label_LocalIP);
		lbl_LocalIP.setText("");
		ServerIP.clear();
		if(receiveThread.isAlive())receiveThread.interrupt();
		if(SendDataThread.isAlive())SendDataThread.interrupt();
		Connected=false;

		//get local IP Address
		try {

			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						lbl_LocalIP.append("你爪机的IP地址\n" + inetAddress.getHostAddress());
						lbl_LocalIP.append("\n探测到的电脑:");
					}
				}
			}
		}
		catch (Exception e) {
		}

		//start listen thread first and then send
		if (!receiveThread.isAlive()) receiveThread.start();

		//send
		try {
			//Broad self Address
			MulticastSocket broadcastSocket = new MulticastSocket(7260);
			InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
			String str = "<Request>";
			byte data[] = str.getBytes();
			broadcastSocket.setBroadcast(true);
			DatagramPacket packet = new DatagramPacket(data, data.length, broadcastAddress, 7260);
			broadcastSocket.send(packet);
			broadcastSocket.close();
		}
		catch (Exception e) {
			new AlertDialog.Builder(this)
					.setTitle("呵呵呵")
					.setMessage(e.toString())
					.show();
		}
	}


	public void Probe(MenuItem item) {
		Probe();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//set full screen
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//get vertical seekbar
 		final VerticalSeekBar vsb_acc = (VerticalSeekBar) MainActivity.this.findViewById(R.id.vsb_Accelerate);
		final VerticalSeekBar vsb_brake = (VerticalSeekBar) MainActivity.this.findViewById(R.id.vsb_Brake);
		final Button btn_HandBrake = (Button) MainActivity.this.findViewById(R.id.button_Handbrake);
		final Button btn_Connect = (Button) MainActivity.this.findViewById(R.id.button_Connect);

		vsb_brake.setOnSeekBarChangeListener(new VerticalSeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
				String s = "A2-" + (128 + progress);
				DataToSend.offer(s);
			}

			@Override
			public void onStartTrackingTouch(VerticalSeekBar vBar) {
				vsb_acc.setProgress(0);
			}

			@Override
			public void onStopTrackingTouch(VerticalSeekBar vBar) {
				vBar.setProgress(128);
			}
		});
		btn_HandBrake.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction()==MotionEvent.ACTION_UP)DataToSend.offer("B01-0");
				if(event.getAction()==MotionEvent.ACTION_DOWN)DataToSend.offer("B01-1");
				return false;
			}
		});
		btn_Connect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Probe();
			}
		});
		//Allow UDP send
		StrictMode.setThreadPolicy(
				new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

		//show sensor data
		SensorManager smg = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor sensor_acc = smg.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		smg.registerListener(new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				TextView lbl_AccData = (TextView) MainActivity.this.findViewById(R.id.label_AccData);
				double x = event.values[SensorManager.DATA_X],
						y = event.values[SensorManager.DATA_Y],
						z = event.values[SensorManager.DATA_Z];
				double d = y / Math.sqrt(x * x + y * y + z * z) * 180;

				//dead zone
				if (Math.abs(d) < 5) d = 0;
				else {
					if (d > 5) d -= 5;
					if (d < -5) d += 5;
				}

				//extreme value
				if (d > 120) d = 128;
				if (d < -120) d = -128;

				//get final msg
				d += 128;
				int feedback = (int) d;
				String sensorDataString = "A1-" + feedback;

				//send thrust data
				int feedback_thrust = 256 - (vsb_acc.getProgress() + vsb_brake.getProgress());
				String thrustDataString = "A2-" + feedback_thrust;

				//put data into send queue
				lbl_AccData.setText("方向盘反馈数据"+feedback);
				if (Connected) {
					DataToSend.offer(sensorDataString);
					DataToSend.offer(thrustDataString);
				}
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		}, sensor_acc, SensorManager.SENSOR_DELAY_GAME);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
