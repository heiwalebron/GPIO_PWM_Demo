package com.friendlyarm;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.friendlyarm.AndroidSDK.GPIOEnum;
import com.friendlyarm.AndroidSDK.HardwareControler;
import com.friendlyarm.Utils.LED;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    HardwareControler hw;

    private Button startPWM;

    private Button stopPWM;

    private ListView listLED;

    private static final String TAG = "GPIODemo";

    private Timer timer = new Timer();

    private int step = 0;

    private int led_gpio_base;

    static int STEP_INIT_GPIO_DIRECTION = 1;

    static int STEP_CLOSE_ALL_LED = 2;

    static int STEP_INIT_VIEW = 3;

    MyCustomAdapter dataAdapter;

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    timer.cancel();
                    // Generate list View from ArrayList
                    displayListView();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private TimerTask init_task = new TimerTask() {
        public void run() {
            Log.d(TAG, "init_task " + step);
            if (step == STEP_INIT_GPIO_DIRECTION) {
                for (int i=0; i<4; i++) {
                    if (HardwareControler.setGPIODirection(led_gpio_base+i, GPIOEnum.OUT) == 0) {
                    } else {
                        Log.v("TimerTask", String.format("setGPIODirection(%d) failed", led_gpio_base+i));
                    }
                }
                step ++;
            } else if (step == STEP_CLOSE_ALL_LED) {
                for (int i=0; i<4; i++) {
                    if (HardwareControler.setGPIOValue(led_gpio_base+i, GPIOEnum.HIGH) == 0) {
                    } else {
                        Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+i));
                    }
                }
                step ++;
            } else if (step == STEP_INIT_VIEW) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        led_gpio_base = 281;

        for (int i = 0; i < 4; i++) {
            int pin = led_gpio_base + i;
            if (HardwareControler.exportGPIOPin(pin) == 0) {
            } else {
                Toast.makeText(this, String.format("exportGPIOPin(%d) failed!", pin),
                        Toast.LENGTH_SHORT).show();
            }
        }

        step = STEP_INIT_GPIO_DIRECTION;
        timer.schedule(init_task, 100, 100);

        startPWM=(Button)findViewById(R.id.start_pwm);
        stopPWM=(Button)findViewById(R.id.stop_pwm);
        listLED=(ListView)findViewById(R.id.list_led);

        startPWM.setOnClickListener(this);
        stopPWM.setOnClickListener(this);

        hw=new HardwareControler();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.start_pwm:

                hw.PWMPlay(1000);

                break;

            case R.id.stop_pwm:

                hw.PWMStop();

                break;
        }
    }
    private void displayListView() {
        Log.d(TAG, "displayListView");

        ArrayList<LED> ledList = new ArrayList<LED>();
        LED led = new LED(0, "LED 1", false);
        ledList.add(led);
        led = new LED(1, "LED 2", false);
        ledList.add(led);
        led = new LED(2, "LED 3", false);
        ledList.add(led);
        led = new LED(3, "LED 4", false);
        ledList.add(led);

        dataAdapter = new MyCustomAdapter(this, R.layout.checkbox_listview_item,
                ledList);
        listLED.setAdapter(dataAdapter);

        listLED.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                LED led = (LED) parent.getItemAtPosition(position);
                if (HardwareControler.setGPIOValue(led_gpio_base+led.code,
                        led.isSelected()?GPIOEnum.LOW:GPIOEnum.HIGH) == 0) {
                } else {
                    Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+led.code));
                }
            }
        });

    }
    public class MyCustomAdapter extends ArrayAdapter<LED> {
        private ArrayList<LED> ledList;
        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<LED> ledList) {
            super(context, textViewResourceId, ledList);
            this.ledList = new ArrayList<LED>();
            this.ledList.addAll(ledList);
        }

        private class ViewHolder {
            CheckBox name;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v(TAG, String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.checkbox_listview_item, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView
                        .findViewById(R.id.checkBox1);
                convertView.setTag(holder);

                holder.name.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        LED led = (LED) cb.getTag();
                        led.setSelected(cb.isChecked());
                        if (HardwareControler.setGPIOValue(led_gpio_base+led.code,
                                led.isSelected() ? GPIOEnum.LOW:GPIOEnum.HIGH) == 0) {
                        } else {
                            Log.v(TAG, String.format("setGPIOValue(%d) failed", led_gpio_base+led.code));
                        }
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            LED led = ledList.get(position);
            holder.name.setText(led.getName());
            holder.name.setChecked(led.isSelected());
            holder.name.setTag(led);

            return convertView;

        }

    }
}
