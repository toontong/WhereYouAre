package io.toontong.where;

import android.content.Context;
import android.util.Log;
 
/**
 * 自定义全局未处理异常捕获器
 * Created  on 13-12-13.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
	private static String TAG = "where.cras";
 
    private static CrashHandler instance;  //单例引用，这里我们做成单例的，因为我们一个应用程序里面只需要一个UncaughtExceptionHandler实例
 
    private CrashHandler() {
    }
 
    public synchronized static CrashHandler getInstance() {  //同步方法，以免单例多线程环境下出现异常
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }
 
    public void init(Context ctx) {  //初始化，把当前对象设置成UncaughtExceptionHandler处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
 
    public void uncaughtException(Thread thread, Throwable ex) {  //当有未处理的异常发生时，就会来到这里。。
        Log.e(TAG, "uncaughtException, thread: " + thread
                + " name: " + thread.getName() + " id: " + thread.getId() + "exception: "
                + ex);

    }
 
 
}
