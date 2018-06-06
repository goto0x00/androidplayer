package dai.android.core.player;

import java.util.Map;

public interface IVideoPlayer {

    /**
     * 设置视频 url，以及 headers
     *
     * @param url     视频地址:  本地或网络视频
     * @param headers 请求 header
     */
    void setUp(String url, Map<String, String> headers);

    /**
     * start player
     */
    void start();

    /**
     * start player at position
     *
     * @param position
     */
    void start(long position);

    /**
     * 重新播放
     * <p>
     * 播放器 暂停、播放错误、播放完成 需要调用此方法重新播放
     * </p>
     */
    void restart();

    /**
     * play pause
     */
    void pause();

    /**
     * play at seek to
     *
     * @param position the position
     */
    void seekTo(long position);

    /**
     * set the volume
     *
     * @param volume the voice volume
     */
    void setVolume(int volume);

    /**
     * set the play speed
     * <p>Only use at IjkPlayer present</p>
     *
     * @param speed the speed
     */
    void setSpeed(float speed);

    /**
     * play from last position
     * <p>
     * <p>true:play from last position</p>
     * <p>false: not play from last position </p>
     *
     * @param fromLastPosition
     */
    void playFormLastPostion(boolean fromLastPosition);

    /***********************************************************************************************
     * the follow function work at player status
     **********************************************************************************************/
    boolean isIdle();

    boolean isPreparing();

    boolean isPrepared();

    boolean isBufferingPlaying();

    boolean isBufferingPaused();

    boolean isPlaying();

    boolean isPaused();

    boolean isError();

    boolean isCompleted();

    /**********************************************************************************************/

    /**
     * get the duration of this video
     * <p>unit: Millisecond</p>
     *
     * @return
     */
    long getDUration();

    /**
     * get current video play position
     *
     * @return
     */
    long getCurrentPosition();

    /**
     * get the video buffer percentage
     *
     * @return
     */
    int getBufferPercentage();

    /**
     * get the video play speed
     *
     * @param speed
     * @return
     */
    float getSpeed(float speed);

    /**
     * release a player
     */
    void releasePlayer();

    /**
     * release all player
     */
    void release();

}
