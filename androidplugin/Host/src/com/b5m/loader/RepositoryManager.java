package com.b5m.loader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.b5m.loader.model.FileSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by boguang on 14/12/23.
 */
public class RepositoryManager {
    static final String STATUS_DONE = "DONE";//已经下载，可以直接加载
    static final String STATUS_IDLE = "IDLE";//未开始下载，
    static final String STATUS_RUNNING = "RUNNING";//正在下载的

    static interface StatusChangeListener {
        void onStatusChanged(FileSpec spec, String status);
    }

    private final Context context;
    private final ConnectivityManager connManager;

    // ./repo/<id>/<md5 or 1>.apk
    // ./repo/<id>/dexout
    private final File repoDir;
    // ./repo/tmp/<id>.<random.4>
    private final File tmpDir;

    private final LinkedList<FileSpec> order = new LinkedList<FileSpec>();
    private final HashMap<String, FileSpec> map = new HashMap<String, FileSpec>();
    private final HashMap<String, String > status = new HashMap<String, String>();
    private final HashMap<String, Integer> require = new HashMap<String, Integer>();
    private final ArrayList<StatusChangeListener> listeners = new ArrayList<StatusChangeListener>();

    private Worker running;

    public RepositoryManager(Context context) {
        this.context = context;

        ConnectivityManager cm = null;
        try {
            cm= (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
            Log.w("Loader","repository start without connectivity manager" , e);
        }
        connManager = cm;

        repoDir = new File(context.getFilesDir(),"repo");
        repoDir.mkdir();
        tmpDir = new File(repoDir,"tmp");
        tmpDir.mkdir();

        File[] tmps = tmpDir.listFiles();
        if (tmps != null) {
            for (File tmp : tmps) {
                tmp.delete();
            }
        }
        disableConnectionReuseIfNecessary();
    }

    void addListener(StatusChangeListener listener) {
        listeners.add(listener);
    }

    void removeListener(StatusChangeListener listener) {
        listeners.remove(listener);
    }

    synchronized int runningCount() {
        int count = 0;
        for (String value : status.values()) {
            if (value == STATUS_RUNNING)
                count++;
        }
        return count;
    }

    synchronized int totalCount() {
        return map.size();
    }

    File getDir(){ return repoDir; }

    synchronized String getStatus(String filed) {
        if (status.get(filed) == null) {
            FileSpec spec = map.get(filed);
            if (null != spec) {
                File file = getPath(spec);
                status.put(filed, file.isFile() ? STATUS_DONE : STATUS_IDLE);
            } else {
                return null;
            }
        }
        return status.get(filed);
    }


    public File getPath(FileSpec fileSpec) {
        File dir = new File(repoDir,fileSpec.getId());
        File path = new File(dir, TextUtils.isEmpty(fileSpec.getMd5()) ? "1.apk" : (fileSpec.getMd5() + ".apk"));
        return  path;
    }

    public synchronized void addFiles(FileSpec[] fileSpecs) {
        for (FileSpec spec : fileSpecs) {
            map.put(spec.getId(),spec);
        }

        for (FileSpec spec : fileSpecs) {
            appendDepsList(map, order, spec.getId());
        }
    }

    public  boolean appendDepsList(List<FileSpec>linkedList,
                                        String filed) {
        return appendDepsList(map,linkedList,filed);
    }

    // return false if missing file or loop deps
    public static boolean appendDepsList(HashMap<String ,FileSpec> map,
                                         List<FileSpec>linkedList,
                                        String filed){
        FileSpec spec = map.get(filed);
        if (null == spec)
            return false;
        if (linkedList.contains(spec))
            return true;
        if (null != spec.getDeps()) {
            for (String dep : spec.getDeps()) {
                if (!appendDepsList(map, linkedList, dep))
                    return false;
            }
        }
        if (linkedList.contains(spec))
            return false;
        linkedList.add(spec);
        return true;
    }

    public synchronized void notifyConnectivityChanged() {
        if (null != running)
            return;
        if (null == pickFromQueue())
            return;
        start();
    }

    synchronized void start() {
        if (null == running) {
            if (status.size() == map.size()) {
                int idleCount = 0;
                for (String value : status.values()) {
                    if (STATUS_IDLE == value)
                        idleCount++;
                }
                if (0 == idleCount)
                    return;
            }
            running = new Worker();
            running.start();
        }
    }

    public synchronized void require(FileSpec... spec) {
        order.removeAll(Arrays.asList(spec));
        for (int i = spec.length - 1; i >= 0 ; i--) {
            FileSpec file = spec[i];
            order.addFirst(file);
            Integer rc = require.get(file.getId());
            if (null == rc)
                require.put(file.getId(),1);
            else
                require.put(file.getId(),rc + 1);
        }

        running = new Worker();
        running.start();
    }

    public synchronized void dismiss(FileSpec... spec) {
        for (FileSpec file : spec) {
            Integer rc = require.get(file.getId());
            if (null != rc && rc > 0) {
                require.put(file.getId(), rc -1 );
            }
        }
    }



    private synchronized FileSpec pickFromQueue() {
        int networkType = -1;
        for (FileSpec spec : order) {
            if (getStatus(spec.getId()) == STATUS_IDLE) {
                Integer rc = require.get(spec.getId());
                if (null != rc && rc > 0)
                    return spec;
                if (spec.getDown() >= FileSpec.DOWN_ALWAYS)
                    return spec;
                if (spec.getDown() <= FileSpec.DOWN_NONE)
                    continue;
                if (networkType <= -1)
                    networkType = getNetWorkType();
                switch (spec.getDown()) {
                    case FileSpec.DOWN_3G:
                        if (networkType >= 2)
                            return spec;
                        break;
                    case FileSpec.DOWN_WIFI:
                        if (networkType >= 3)
                            return spec;
                        break;
                }
            }
        }

        return null;
    }


    public int getNetWorkType() {
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {

            switch (info.getSubtype()){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return 1; //2g
                default:
                    return 2; //3g
            }
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            return 3; //wifi
        }
        return 0; //unknown
    }

    private void disableConnectionReuseIfNecessary() {
        try {
            if (Integer.getInteger(Build.VERSION.SDK) < 8) {
                System.setProperty("http:keepAlive", "false");
            }
        } catch (Exception e ) {

        }
    }

    private class Worker extends Thread {
        private int faileCounter = 0;

        @Override
        public void run() {
            FileSpec current = null;
            while (this == running && (current = pickFromQueue()) != null) {

                final FileSpec fCurrent = current;
                Log.i("Loader","start download" + fCurrent.getId() + " from " +fCurrent.getUrl());

                long startMs = SystemClock.currentThreadTimeMillis();

                status.put(fCurrent.getId(),STATUS_RUNNING);

                for (StatusChangeListener ls : listeners) {
                    ls.onStatusChanged(fCurrent,STATUS_RUNNING);
                }

                String rnd = Integer.toHexString(new Random(System.currentTimeMillis()).nextInt(0xf000) + 0x1000);
                File f = new File(tmpDir, fCurrent.getId() + "." + rnd);
                boolean succeed = false;

                /*下载*/

                try {
                    URL url = new URL(fCurrent.getUrl());
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    conn.setConnectTimeout(15000);//15秒
                    InputStream ins = conn.getInputStream();
                    FileOutputStream fos = new FileOutputStream(f);
                    byte buffer[] = new byte[4*1024];
                    int l ;
                    while (-1 != (l = ins.read(buffer,0,buffer.length))) {
                        fos.write(buffer,0,l);
                    }
                    fos.close();
                    ins.close();
                    conn.disconnect();
                    succeed = true;

                } catch (Exception e) {
                    Log.w("Loader","failed to  download" + fCurrent.getId() + " from " +fCurrent.getUrl(), e);
                }

                /*文件比对, MD5检验*/

                if (f.length() > 0 && !TextUtils.isEmpty(fCurrent.getMd5())) {
                    succeed = false;
                    try {
                        MessageDigest m = MessageDigest.getInstance("MD5");
                        m.reset();
                        FileInputStream fis = new FileInputStream(f);
                        byte buffer[] = new byte[4*1024];
                        int l ;
                        while (-1 != (l = fis.read(buffer,0, buffer.length))) {
                            m.update(buffer, 0 , l);
                        }
                        fis.close();
                        String md5 = byteArrayToHexString(m.digest());
                        succeed = fCurrent.getMd5().equals(md5);
                        if (!succeed)
                            Log.e("Loader","failed to  match " + fCurrent.getId() + " MD5 "+ fCurrent.getMd5() + " md5  " + md5);

                    } catch (Exception e) {
                        Log.w("Loader","failed to  verify file " + f.getAbsolutePath(), e);
                    }
                }

                /*文件迁移 */
                File path = getPath(fCurrent);
                if (succeed) {
                    path.getParentFile().mkdir();
                    succeed = f.renameTo(path);
                    if (!succeed)
                        Log.e("Loader","failed to  move" + fCurrent.getId() + " from "+ f.getAbsoluteFile()
                                + " to  " + path.getAbsolutePath());
                }

                if (!succeed)
                    f.delete();// delete tmp file if not succeed;

                succeed = path.isFile();
                final String newStatus = succeed ? STATUS_DONE : STATUS_IDLE;
                status.put(fCurrent.getId(), newStatus);
                if (succeed) {
                    long elapse = SystemClock.currentThreadTimeMillis() - startMs;
                    Log.i("Loader",current.getId() +  " (" + path.length() + " bytes ) finished in " + elapse + "ms");
                    if (faileCounter > 0)
                        faileCounter--;
                } else  {
                    order.remove(current);
                    order.addLast(current);
                    faileCounter++;
                }

                /*状态通知*/
                for (StatusChangeListener ls : listeners) {
                    ls.onStatusChanged(fCurrent,newStatus);
                }

                /*超过3次放弃*/
                if (faileCounter >= 3) {
                    Log.w("loader", " download fail 3 times, abort");
                    break;
                }

             }

            synchronized (RepositoryManager.this) {
                if (this == running) {
                    running = null;
                }
            }

        }
    }


    private static final String[] hexDigists = {"0","1","2","3",
            "4","5","6","7","8","9","a","b","c","d","e","f"};

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 0x100 + n;
        int d1 = n>>4;
        int d2 = n & 0xF;
        return hexDigists[d1]+hexDigists[d2];
    }

    public static String byteArrayToHexString(byte[] b) {
        StringBuilder resultSb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

}
