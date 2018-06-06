package dai.android.player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import dai.android.core.router.ActionData;
import dai.android.core.router.ActionResult;
import dai.android.player.data.Employee;
import dai.android.player.data.Normal;
import dai.android.player.data.Person;

public class TaskService extends Service {
    private static final String TAG = TaskService.class.getSimpleName();

    public TaskService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind the TaskService service");
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand the TaskService service");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new ICall.Stub() {
        @Override
        public ActionResult invoke(ActionData data) throws RemoteException {

            ActionResult result = new ActionResult(ActionResult.RESULT_OK);

            if (null != data) {
                result.setCallId(data.getCallId());

                switch (data.getCallId()) {
                    case R.id.rbLeft_0x01: { // demo 1
                        result.setValue("I'm Patrick");
                        result.setCode(1);
                    }
                    break;

                    case R.id.rbLeft_0x02: { // demo 2
                        Person person = new Person("戴金国", 30);
                        result.setValue(person);
                        result.setCode(2);
                    }
                    break;

                    case R.id.rbLeft_0x03: { //demo3
                        Person[] persons = new Person[2];
                        persons[0] = new Person("李磊", 30);
                        persons[1] = new Person("马化腾", 40);
                        result.setValue(persons);
                        result.setCode(3);
                    }
                    break;

                    case R.id.rbLeft_0x04: { //demo4
                        Employee[] employee = new Employee[2];
                        employee[0] = new Employee("Patrick", 10, "MySoft");
                        employee[1] = new Employee("Bile", 60, "Microsoft");
                        result.setValue(employee);
                        result.setCode(4);
                    }
                    break;

                    case R.id.rbLeft_0x05: { //demo5
                        Person[] employee = new Person[2];
                        employee[0] = new Employee("Bile", 60, "Microsoft");
                        employee[1] = new Person("Patrick", 10);
                        result.setValue(employee);
                        result.setCode(5);
                    }
                    break;

                    case R.id.rbLeft_0x06: {  // demo6 fail demo
                        // this normal Object not implement Parcelable or
                        // java.io.Serializable
                        Normal normal = new Normal();
                        normal.age = 2;
                        normal.name = "demo5";
                        normal.makeArray();
                        result.setValue(normal);
                        result.setCode(6);
                    }
                }

            }


            return result;
        }
    };
}
