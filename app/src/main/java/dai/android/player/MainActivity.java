package dai.android.player;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import java.util.ArrayList;

import dai.android.core.router.ActionData;
import dai.android.core.router.ActionResult;
import dai.android.player.data.Employee;
import dai.android.player.data.Normal;
import dai.android.player.data.Person;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ICall mCall = null;

    private ArrayList<RadioButton> mRadioButtonsLeft = new ArrayList<>();
    private ArrayList<RadioButton> mRadioButtonsRight = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x01));
        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x02));
        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x03));
        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x04));
        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x05));
        mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x06));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x07));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x08));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x09));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0A));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0B));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0C));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0D));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0E));
        //mRadioButtonsLeft.add((RadioButton) findViewById(R.id.rbLeft_0x0F));

        mRadioButtonsRight.add((RadioButton) findViewById(R.id.rb_right_0x01));
        mRadioButtonsRight.add((RadioButton) findViewById(R.id.rb_right_0x02));
        mRadioButtonsRight.add((RadioButton) findViewById(R.id.rb_right_0x03));
        mRadioButtonsRight.add((RadioButton) findViewById(R.id.rb_right_0x04));

        findViewById(R.id.btnUnBind).setOnClickListener(mOnClickListener);
        findViewById(R.id.btnBind).setOnClickListener(mOnClickListener);
        findViewById(R.id.btnLeft).setOnClickListener(mOnClickListener);
        findViewById(R.id.btnRight).setOnClickListener(mOnClickListener);
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (R.id.btnBind == v.getId()) {
                if (null == mCall || !mIsServiceBind) {
                    Log.d(TAG, "R.id.btnBind: now bind service");
                    bindService();
                }
            }

            if (R.id.btnUnBind == v.getId()) {
                if (null != mCall && mIsServiceBind) {
                    Log.d(TAG, "R.id.btnUnBind: now unbind service");
                    unbindService(mConnection);
                    mIsServiceBind = false;
                    mCall = null;
                }
            }

            if (R.id.btnLeft == v.getId()) {
                int index = -1;
                for (int i = 0; i < mRadioButtonsLeft.size(); ++i) {
                    if (mRadioButtonsLeft.get(i).isChecked()) {
                        index = mRadioButtonsLeft.get(i).getId();
                        break;
                    }
                }

                if (-1 == index) {
                    Log.d(TAG, "No radio button click in left");
                    return;
                }

                ActionData data = new ActionData(true);
                data.setCallId(index);

                if (null == mCall || !mIsServiceBind) {
                    bindService();
                }

                ActionResult result = null;
                try {
                    result = mCall.invoke(data);
                } catch (RemoteException e) {
                    Log.d(TAG, "RemoteException:", e);
                }

                if (null != result) {
                    Log.d(TAG, "Response, code   : " + result.getCode());
                    Log.d(TAG, "Response, call id: " + result.getCallId());

                    switch (result.getCallId()) {
                        case R.id.rbLeft_0x01: {
                            String str = (String) result.getValue();
                            Log.d(TAG, "rbLeft_0x01: " + str);
                            break;
                        }

                        case R.id.rbLeft_0x02: {
                            Person person = (Person) result.getValue();
                            Log.d(TAG, "rbLeft_0x02: " + person);
                            break;
                        }

                        case R.id.rbLeft_0x03: {
                            Person[] people = (Person[]) result.getValue();
                            if (null != people && people.length > 0) {
                                for (Person p : people) {
                                    Log.d(TAG, "rbLeft_0x03: " + p);
                                }
                            }
                            break;
                        }

                        case R.id.rbLeft_0x04: {
                            Employee[] people = (Employee[]) result.getValue();
                            if (null != people && people.length > 0) {
                                for (Person p : people) {
                                    Log.d(TAG, "rbLeft_0x04: " + p);
                                }
                            }
                            break;
                        }

                        case R.id.rbLeft_0x05: {
                            Person[] people = (Person[]) result.getValue();
                            if (null != people && people.length > 0) {
                                for (Person P : people) {
                                    if (P instanceof Employee) {
                                        Log.d(TAG, "rbLeft_0x05: " + (Employee) P);
                                    } else {
                                        Log.d(TAG, "rbLeft_0x05: " + P);
                                    }
                                }
                            }
                            break;
                        }

                        case R.id.rbLeft_0x06: {
                            Normal normal = (Normal) result.getValue();
                            break;
                        }
                    }
                }

            }


            if (R.id.btnRight == v.getId()) {
                int index = -1;
                for (int i = 0; i < mRadioButtonsRight.size(); ++i) {
                    if (mRadioButtonsRight.get(i).isChecked()) {
                        index = mRadioButtonsRight.get(i).getId();
                        break;
                    }
                }

                if (-1 == index) {
                    Log.d(TAG, "No radio button click in right");
                    return;
                }

                switch (index) {
                    case R.id.rb_right_0x01: {
                        break;
                    }

                    case R.id.rb_right_0x02: {
                        Intent intent = new Intent(MainActivity.this, FactoryTestActivity.class);
                        startActivity(intent);
                        break;
                    }
                }
            }

        }
    };

    private void bindService() {
        Intent intent = new Intent(MainActivity.this, TaskService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private boolean mIsServiceBind = false;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCall = ICall.Stub.asInterface(service);
            mIsServiceBind = true;

            Log.d(TAG, "onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCall = null;
            mIsServiceBind = false;

            Log.d(TAG, "onServiceDisconnected");
        }
    };
}
