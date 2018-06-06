package dai.android.player.service;

import android.os.IBinder;

import dai.android.core.router.AbstractFactoryService;

public class FactoryTestService extends AbstractFactoryService {

    public FactoryTestService() {
    }

    @Override
    protected Class getCurrentServiceClass() {
        return this.getClass();
    }

    @Override
    protected IBinder getBinder() {
        return null;
    }


}
