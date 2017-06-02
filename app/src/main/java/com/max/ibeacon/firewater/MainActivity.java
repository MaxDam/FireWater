package com.max.ibeacon.firewater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.max.ibeacon.firewater.adapter.BeaconListAdapter;
import com.max.ibeacon.firewater.common.RangeSeekBar;
import com.max.ibeacon.firewater.common.Utils;

public class MainActivity extends Activity implements BeaconConsumer {

	protected static final String TAG = "MainActivity";
	private BeaconManager beaconManager;
	private TextView message;
    private TextView range;
	private ImageView img;

    private float maxDistance = 1.0f;
    private float minDistance = 0.3f;

    private String selectedUuid = null;
    public AlertDialog alertDialog;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		img = (ImageView)findViewById(R.id.img);
		message = (TextView)findViewById(R.id.message);
        range = (TextView)findViewById(R.id.range);

        //cliccando sul messaggio azzera l'uuid selezionato
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedUuid = null;
            }
        });

        //barra per settare le soglie
        RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(0, 300, this);
        seekBar.setSelectedMaxValue((int)(maxDistance*100));
        seekBar.setSelectedMinValue((int)(minDistance*100));
        range.setText("Range: " + minDistance + " - " + maxDistance + " m");
        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                maxDistance = (float)maxValue / 100;
                minDistance = (float)minValue / 100;
                range.setText("Range: " + minDistance + " - " + maxDistance + " m");
            }
        });
        ViewGroup layout = (ViewGroup)findViewById(R.id.main);
        layout.addView(seekBar);

        //attiva il bluetooth
        Utils.setBluetooth(true);

        //inizializza il beacon scanner
		beaconManager = BeaconManager.getInstanceForApplication(this);
		beaconManager.getBeaconParsers().add(new BeaconParser().
	               setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
		beaconManager.bind(this);
	}
	
	@Override 
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);

        Utils.setBluetooth(false);
    }
	
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setMonitorNotifier(new MonitorNotifier() {
	        @Override
	        public void didEnterRegion(Region region) {
	          Log.i(TAG, "I just saw an beacon for the first time!");       
	        }
	
	        @Override
	        public void didExitRegion(Region region) {
	          Log.i(TAG, "I no longer see an beacon");
	        }
	
	        @Override
	        public void didDetermineStateForRegion(int state, Region region) {
	            Log.i(TAG, "I have just switched from seeing/not seeing beacons: "+state);      
	        }
        });
	
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override 
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {

                    if(selectedUuid == null) {

                        //prepara il model della popup scelta dettaglio
                        final List<Beacon> beaconList = new ArrayList<Beacon>(beacons);
                        final BeaconListAdapter adapter = new BeaconListAdapter(MainActivity.this, beaconList);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(alertDialog == null || !alertDialog.isShowing()) {
                                    //costruisce e visualizza la scelta del dispositivo
                                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                                    dialogBuilder.setTitle(R.string.beacon_list_title);
                                    dialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int item) {
                                            Beacon beacon = adapter.getItem(item);
                                            selectedUuid = beacon.getId1() + "-" + beacon.getId2() + "-" + beacon.getId3();
                                        }
                                    });
                                    alertDialog = dialogBuilder.create();
                                    alertDialog.show();
                                }
                                else {
                                    BeaconListAdapter adapter = (BeaconListAdapter)alertDialog.getListView().getAdapter();
                                    adapter.refill(beaconList);
                                }
                            }
                        });
                    }

                    //scorre i dispositivi trovati
                    for(final Beacon beacon : beacons) {

                        //ottiene l'UUID
                        final String uuid = beacon.getId1()+"-"+beacon.getId2()+"-"+beacon.getId3();

                        //se l'UUID non e' quello selezionato lo salta
                        if(!uuid.equals(selectedUuid)) continue;

                        //double dinstance = beacon.getDistance();
                        final double distance = Beacon.getDistanceCalculator().calculateDistance(beacon.getTxPower(), beacon.getRssi());

                    	runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            	//visualizza i valori del segnale
                            	StringBuffer msg = new StringBuffer();
                            	msg.append("UUID: " + uuid).append("\n");
                            	msg.append("RSSI: " + beacon.getRssi()).append("\n");
                            	msg.append("TxPower: " + beacon.getTxPower()).append("\n");
                            	msg.append(String.format("Distance: %.2fm", distance)).append("\n");
                            	message.setText(msg.toString());
                            	
                            	//visualizza l'icona di acqua o fuoco
                            	if(distance > maxDistance) {
                            		img.setImageResource(R.drawable.water);
                            	} else if(distance > minDistance) {
                            		img.setImageResource(R.drawable.litlefire);
                            	} else {
                            		img.setImageResource(R.drawable.fire);
                            	}
                            }
                    	});
                    	break;
                    }
                }
            }
        });
        
        try {
            //beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
