// ICall.aidl
package dai.android.player;

import dai.android.core.router.ActionData;
import dai.android.core.router.ActionResult;

interface ICall {

    ActionResult invoke( in ActionData data );

}
