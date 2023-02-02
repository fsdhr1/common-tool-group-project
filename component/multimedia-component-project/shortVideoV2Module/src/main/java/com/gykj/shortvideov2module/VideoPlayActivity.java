package com.gykj.shortvideov2module;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileUtils;

/**
 * @author 贺鑫海
 */
public class VideoPlayActivity extends AppCompatActivity {
    private final String Tag = VideoPlayActivity.class.getSimpleName();
    private MediaPlayer mMediaPlayer;
    private Surface surface;

    private ImageView videoImage;
    private SeekBar seekBar;
    private Handler handler=new Handler();

    private final Runnable mTicker = new Runnable(){
        @Override
        public void run() {
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);

            handler.postAtTime(mTicker,next);//延迟一秒再次执行runnable,就跟计时器一样效果

            if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){
                seekBar.setProgress(mMediaPlayer.getCurrentPosition());//更新播放进度
            }
        }
    };
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        videoPath = getIntent().getStringExtra("videoPath");
        TextureView textureView=(TextureView) findViewById(R.id.textureview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);

        videoImage=(ImageView) findViewById(R.id.video_image);

        videoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMediaPlayer.start();
                handler.post(mTicker);//更新进度
                videoImage.setVisibility(View.GONE);
            }
        });

        seekBar= (SeekBar) findViewById(R.id.seekbar);
        //seekbar改变监听
        seekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener=new TextureView.SurfaceTextureListener() {
        //SurfaceTexture可用
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width,int height) {
            surface=new Surface(surfaceTexture);
            new PlayerVideoThread().start();//开启一个线程去播放视频
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,int height) {//尺寸改变
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {//销毁
            surface=null;
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer=null;
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {//更新
        }
    };

    private class PlayerVideoThread extends Thread{
        @Override
        public void run(){
            try {
                mMediaPlayer= new MediaPlayer();
                Uri uri =Uri.fromFile(FileUtils.getFileByPath(videoPath));
                mMediaPlayer.setDataSource(VideoPlayActivity.this,uri);
                mMediaPlayer.setSurface(surface);//设置渲染画板
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);//设置播放类型
                mMediaPlayer.setOnCompletionListener(onCompletionListener);//播放完成监听
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {//预加载监听
                    @Override
                    public void onPrepared(MediaPlayer mp){//预加载完成
                        videoImage.setVisibility(View.GONE);//隐藏图片
                        mMediaPlayer.start();//开始播放
                        handler.post(mTicker);//更新进度
                        seekBar.setMax(mMediaPlayer.getDuration());//设置总进度

                    }
                });
                mMediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {//进度改变
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {//开始拖动seekbar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {//停止拖动seekbar
            if(mMediaPlayer!=null&&mMediaPlayer.isPlaying()){//播放中
                mMediaPlayer.seekTo(seekBar.getProgress());
            }
        }
    };

    private MediaPlayer.OnCompletionListener onCompletionListener=new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {//播放完成
            videoImage.setVisibility(View.VISIBLE);
            seekBar.setProgress(0);
            handler.removeCallbacks(mTicker);//删除执行的Runnable 终止计时器
        }
    };

}
